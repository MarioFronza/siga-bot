package br.com.mariofronza.sigabot.persistence

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.notification.JooqOutbox
import br.com.mariofronza.sigabot.persistence.sync.JooqSyncQueue
import br.com.mariofronza.sigabot.persistence.user.JooqUserRepository
import br.com.mariofronza.sigabot.usecases.academic.port.SyncJob
import br.com.mariofronza.sigabot.usecases.notification.port.OutboxMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.jooq.test.autoconfigure.JooqTest
import org.springframework.context.annotation.Import
import java.time.Duration
import java.time.Instant

@JooqTest
@ImportAutoConfiguration(FlywayAutoConfiguration::class)
@Import(JooqUserRepository::class, JooqSyncQueue::class, JooqOutbox::class)
class QueueAndOutboxTest : FunSpec() {

    @Autowired lateinit var users: JooqUserRepository
    @Autowired lateinit var queue: JooqSyncQueue
    @Autowired lateinit var outbox: JooqOutbox

    private val now = Instant.parse("2026-11-20T12:00:00Z")
    private val lease = Duration.ofMinutes(10)

    private fun student(name: String, active: Boolean = true) = users.save(
        User.registerStudent(Email.of("$name@test.com"), "hash", AcademicAccount(InstitutionCode.UDESC, name, name.toByteArray(), "password"))
            .copy(isActive = active)
    )

    init {
        extension(SpringExtension())

        test("claims a due job once while its lease lasts") {
            val ana = student("ana")
            queue.enqueue(ana.id, now)

            queue.claimDue(now, 10, lease) shouldBe listOf(SyncJob(ana.id, 0))
            queue.claimDue(now.plusSeconds(60), 10, lease).shouldBeEmpty()
            queue.claimDue(now.plus(lease).plusSeconds(1), 10, lease) shouldBe listOf(SyncJob(ana.id, 0))
        }

        test("leaves out jobs that are not due and inactive students") {
            queue.enqueue(student("later").id, now.plusSeconds(3600))
            queue.enqueue(student("gone", active = false).id, now)

            queue.claimDue(now, 10, lease).shouldBeEmpty()
            queue.countDue(now) shouldBe 0
        }

        test("claims the most overdue jobs first, up to the limit") {
            val ana = student("ana")
            val bia = student("bia")
            queue.enqueue(ana.id, now.minusSeconds(60))
            queue.enqueue(bia.id, now.minusSeconds(600))

            queue.claimDue(now, 1, lease) shouldBe listOf(SyncJob(bia.id, 0))
        }

        test("reschedules a completed job and counts the attempts of a failing one") {
            val ana = student("ana")
            queue.enqueue(ana.id, now)
            queue.claimDue(now, 10, lease)
            queue.failed(ana.id, "SIGA did not answer", now.plusSeconds(300))

            queue.claimDue(now.plusSeconds(300), 10, lease) shouldBe listOf(SyncJob(ana.id, 1))

            queue.completed(ana.id, now.plusSeconds(3600))
            queue.claimDue(now.plusSeconds(3599), 10, lease).shouldBeEmpty()
            queue.claimDue(now.plusSeconds(3600), 10, lease) shouldBe listOf(SyncJob(ana.id, 0))
        }

        test("keeps a single job per student when enqueued again") {
            val ana = student("ana")
            queue.enqueue(ana.id, now.plusSeconds(3600))
            queue.enqueue(ana.id, now)

            queue.countDue(now) shouldBe 1
        }

        test("hands out pending pushes in order, once while their lease lasts") {
            val ana = student("ana")
            val now = Instant.now()
            outbox.add(ana.id, "primeira")
            outbox.add(ana.id, "segunda")

            outbox.claimPending(now, 10, lease, maxAttempts = 3).map(OutboxMessage::message) shouldBe listOf("primeira", "segunda")
            outbox.claimPending(now.plusSeconds(60), 10, lease, maxAttempts = 3).shouldBeEmpty()
            outbox.claimPending(now.plus(lease).plusSeconds(1), 10, lease, maxAttempts = 3).map(OutboxMessage::message) shouldBe listOf("primeira", "segunda")
        }

        test("retries a failed push only when it is due, and gives up after the last attempt") {
            val ana = student("ana")
            val now = Instant.now()
            outbox.add(ana.id, "oi")
            val push = outbox.claimPending(now, 10, lease, maxAttempts = 2).single()

            outbox.failed(push.id, retryAt = now.plusSeconds(30))
            outbox.claimPending(now.plusSeconds(29), 10, lease, maxAttempts = 2).shouldBeEmpty()
            outbox.claimPending(now.plusSeconds(30), 10, lease, maxAttempts = 2).single().attempts shouldBe 1

            outbox.failed(push.id, retryAt = now.plusSeconds(60))
            outbox.claimPending(now.plusSeconds(3600), 10, lease, maxAttempts = 2).shouldBeEmpty()
            outbox.countPending(maxAttempts = 2) shouldBe 0
        }

        test("stops handing out a delivered push") {
            val ana = student("ana")
            val now = Instant.now()
            outbox.add(ana.id, "oi")
            outbox.delivered(outbox.claimPending(now, 10, lease, maxAttempts = 3).single().id)

            outbox.claimPending(now.plus(lease).plusSeconds(1), 10, lease, maxAttempts = 3).shouldBeEmpty()
        }
    }
}
