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

class RunDueSyncsTest : FunSpec({

    val now = Instant.parse("2026-11-20T12:00:00Z")
    val queue: SyncQueue = mockk(relaxed = true)
    val users: UserRepository = mockk()
    val syncStudentGrades: SyncStudentGrades = mockk(relaxed = true)
    val observer: SyncObserver = mockk(relaxed = true)
    val runDueSyncs = RunDueSyncs(queue, users, syncStudentGrades, observer, Clock.fixed(now, ZoneOffset.UTC))

    fun student(name: String) =
        User.registerStudent(Email.of("$name@test.com"), "hash", AcademicAccount(InstitutionCode.UDESC, name, byteArrayOf(1), "password"))

    val ana = student("ana")
    val bia = student("bia")

    beforeTest {
        clearMocks(queue, syncStudentGrades, observer, answers = false)
        every { users.findById(ana.id) } returns ana
        every { users.findById(bia.id) } returns bia
    }

    test("syncs every claimed student and schedules the next sync an interval later") {
        every { queue.claimDue(now, 8, Duration.ofMinutes(10)) } returns listOf(SyncJob(ana.id, 0), SyncJob(bia.id, 0))

        runDueSyncs(SyncScope.Latest) shouldBe 2

        verify { syncStudentGrades(ana, SyncScope.Latest) }
        verify { syncStudentGrades(bia, SyncScope.Latest) }
        verify { queue.completed(ana.id, now.plus(Duration.ofHours(1))) }
        verify { queue.completed(bia.id, now.plus(Duration.ofHours(1))) }
    }

    test("backs off a failing student without stopping the others") {
        every { queue.claimDue(any(), any(), any()) } returns listOf(SyncJob(ana.id, 2), SyncJob(bia.id, 0))
        every { syncStudentGrades(ana, any()) } throws SigaUnavailableException("SIGA did not answer")

        runDueSyncs(SyncScope.Latest)

        verify { queue.failed(ana.id, "SIGA did not answer", now.plus(Duration.ofMinutes(20))) }
        verify { queue.completed(bia.id, any()) }
        verify { observer.failed(any<SigaUnavailableException>()) }
    }

    test("doubles the retry delay up to a ceiling") {
        runDueSyncs.retryDelay(1) shouldBe Duration.ofMinutes(5)
        runDueSyncs.retryDelay(4) shouldBe Duration.ofMinutes(40)
        runDueSyncs.retryDelay(30) shouldBe Duration.ofHours(6)
    }
})
