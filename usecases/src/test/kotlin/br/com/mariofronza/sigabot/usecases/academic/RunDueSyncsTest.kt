package br.com.mariofronza.sigabot.usecases.academic

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.academic.port.SigaUnavailableException
import br.com.mariofronza.sigabot.usecases.academic.port.SyncJob
import br.com.mariofronza.sigabot.usecases.academic.port.SyncObserver
import br.com.mariofronza.sigabot.usecases.academic.port.SyncQueue
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class RunDueSyncsTest : FunSpec({

    val now = Instant.parse("2026-11-20T12:00:00Z")
    val clock = Clock.fixed(now, ZoneOffset.UTC)
    val lease = Duration.ofMinutes(10)
    val queue: SyncQueue = mockk(relaxed = true)
    val users: UserRepository = mockk()
    val syncStudentGrades: SyncStudentGrades = mockk(relaxed = true)
    val observer: SyncObserver = mockk(relaxed = true)

    fun student(name: String) =
        User.registerStudent(Email.of("$name@test.com"), "hash", AcademicAccount(InstitutionCode.UDESC, name, byteArrayOf(1), "password"))

    val ana = student("ana")
    val bia = student("bia")
    val caio = student("caio")

    fun runDueSyncs(concurrency: Int = 8) =
        RunDueSyncs(queue, users, syncStudentGrades, observer, clock, RunDueSyncs.Settings(concurrency = concurrency))

    beforeTest {
        clearMocks(queue, syncStudentGrades, observer, answers = false)
        listOf(ana, bia, caio).forEach { every { users.findById(it.id) } returns it }
    }

    test("syncs every claimed student and schedules the next sync an interval later") {
        every { queue.claimDue(now, 8, lease) } returns listOf(SyncJob(ana.id, 0), SyncJob(bia.id, 0))

        runDueSyncs().use { it(SyncScope.Latest) shouldBe 2 }

        verify { syncStudentGrades(ana, SyncScope.Latest) }
        verify { queue.completed(ana.id, now, now.plus(Duration.ofHours(1)), null) }
        verify { queue.completed(bia.id, now, now.plus(Duration.ofHours(1)), null) }
    }

    test("runs the scope a student asked for instead of the default one") {
        every { queue.claimDue(now, 8, lease) } returns listOf(SyncJob(ana.id, 0, requested = SyncScope.Everything))

        runDueSyncs().use { it(SyncScope.Latest) }

        verify { syncStudentGrades(ana, SyncScope.Everything) }
        verify { queue.completed(ana.id, now, now.plus(Duration.ofHours(1)), SyncScope.Everything) }
    }

    test("backs off a failing student without stopping the others") {
        every { queue.claimDue(any(), any(), any()) } returns listOf(SyncJob(ana.id, 2), SyncJob(bia.id, 0))
        every { syncStudentGrades(ana, any()) } throws SigaUnavailableException("SIGA did not answer")

        runDueSyncs().use { it(SyncScope.Latest) }

        verify { queue.failed(ana.id, "SIGA did not answer", now.plus(Duration.ofMinutes(20))) }
        verify { queue.completed(bia.id, any(), any(), any()) }
        verify { observer.failed(any<SigaUnavailableException>()) }
    }

    test("fills a freed slot while a slow student is still syncing") {
        val slowStarted = CountDownLatch(1)
        val releaseSlow = CountDownLatch(1)
        every { syncStudentGrades(ana, any()) } answers { slowStarted.countDown(); releaseSlow.await(5, TimeUnit.SECONDS); emptyList() }
        every { queue.claimDue(now, 2, lease) } returns listOf(SyncJob(ana.id, 0), SyncJob(bia.id, 0))
        every { queue.claimDue(now, 1, lease) } returns listOf(SyncJob(caio.id, 0))

        runDueSyncs(concurrency = 2).use { run ->
            run(SyncScope.Latest) shouldBe 2
            slowStarted.await(5, TimeUnit.SECONDS)
            verify(timeout = 5_000) { queue.completed(bia.id, any(), any(), any()) }

            run(SyncScope.Latest) shouldBe 1

            verify(timeout = 5_000) { queue.completed(caio.id, any(), any(), any()) }
            verify(exactly = 0) { queue.completed(ana.id, any(), any(), any()) }
            releaseSlow.countDown()
        }

        verify { queue.completed(ana.id, any(), any(), any()) }
    }

    test("claims nothing while every slot is busy") {
        val release = CountDownLatch(1)
        every { syncStudentGrades(ana, any()) } answers { release.await(5, TimeUnit.SECONDS); emptyList() }
        every { queue.claimDue(now, 1, lease) } returns listOf(SyncJob(ana.id, 0))

        runDueSyncs(concurrency = 1).use { run ->
            run(SyncScope.Latest)
            run(SyncScope.Latest) shouldBe 0
            release.countDown()
        }

        verify(exactly = 1) { queue.claimDue(any(), any(), any()) }
    }

    test("gives the slots back when claiming fails") {
        every { queue.claimDue(any(), any(), any()) } throws IllegalStateException("database down") andThen listOf(SyncJob(ana.id, 0))

        runDueSyncs(concurrency = 2).use { run ->
            shouldThrow<IllegalStateException> { run(SyncScope.Latest) }
            run(SyncScope.Latest) shouldBe 1
        }

        verify { queue.claimDue(now, 2, lease) }
    }

    test("doubles the retry delay up to a ceiling") {
        runDueSyncs().use {
            it.retryDelay(1) shouldBe Duration.ofMinutes(5)
            it.retryDelay(4) shouldBe Duration.ofMinutes(40)
            it.retryDelay(30) shouldBe Duration.ofHours(6)
        }
    }
})
