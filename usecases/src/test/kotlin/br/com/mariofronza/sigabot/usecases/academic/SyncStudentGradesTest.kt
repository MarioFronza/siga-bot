package br.com.mariofronza.sigabot.usecases.academic

import br.com.mariofronza.sigabot.entity.catalog.SubjectOffering
import br.com.mariofronza.sigabot.entity.enrollment.Attendance
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.enrollment.Grade
import br.com.mariofronza.sigabot.entity.enrollment.SemesterOutcome
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.academic.port.AcademicRecord
import br.com.mariofronza.sigabot.usecases.academic.port.AcademicSystem
import br.com.mariofronza.sigabot.usecases.academic.port.EnrollmentRepository
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import br.com.mariofronza.sigabot.usecases.identity.port.SecretCipher
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import br.com.mariofronza.sigabot.usecases.notification.port.PushSender
import br.com.mariofronza.sigabot.usecases.shared.TransactionRunner
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class SyncStudentGradesTest : FunSpec({

    val users: UserRepository = mockk(relaxed = true)
    val enrollments: EnrollmentRepository = mockk(relaxed = true)
    val siga: AcademicSystem = mockk()
    val push: PushSender = mockk(relaxed = true)
    val cipher: SecretCipher = mockk()
    var committed = false
    val transaction = object : TransactionRunner {
        override fun <T> inTransaction(block: () -> T): T = block().also { committed = true }
    }
    val sync = SyncStudentGrades(users, enrollments, listOf(siga), cipher, push, transaction)

    val period = AcademicPeriod(2026, 1)
    val user = User.registerStudent(Email.of("ana@test.com"), "hash", AcademicAccount(InstitutionCode.UDESC, "login", byteArrayOf(1), "password"))
    val offering = SubjectOffering(InstitutionCode.UDESC, period, "Engenharia de Software", "ESO-1", "Banco de Dados", 72)

    fun outcome(grade: Double) = SemesterOutcome(Grade(grade), 0, Attendance(100.0), "Aprovado")
    fun stored(grade: Double) = Enrollment(user.id, offering, Grade(grade), 0, Attendance(100.0), "Aprovado")
    fun sigaReports(grade: Double) {
        every { siga.fetch(AcademicSystem.Credentials("plain-login", "plain-password"), SyncScope.Period(period)) } returns
            AcademicRecord("Ana Souza", listOf(period), listOf(AcademicRecord.Result(offering, outcome(grade))))
    }

    beforeTest {
        clearMocks(users, enrollments, push, answers = false)
        committed = false
        every { siga.institution } returns InstitutionCode.UDESC
        every { cipher.decrypt("login") } returns "plain-login"
        every { cipher.decrypt("password") } returns "plain-password"
    }

    test("stores what SIGA reports in one transaction and answers the synced periods") {
        sigaReports(8.0)
        every { enrollments.find(user.id, offering) } returns null
        val saved = slot<Enrollment>()
        every { enrollments.save(capture(saved)) } returns Unit

        sync(user, SyncScope.Period(period)) shouldBe listOf(period)

        saved.captured shouldBe stored(8.0)
        verify { users.save(user.rename("Ana Souza")) }
        committed shouldBe true
    }

    test("pushes a notification when a stored grade changes") {
        sigaReports(9.5)
        every { enrollments.find(user.id, offering) } returns stored(0.0)

        sync(user, SyncScope.Period(period))

        verify { push.sendTo(user.id, "Ana, você possui uma nota nova da matéria Banco de Dados") }
    }

    test("stays quiet when the grade did not change") {
        sigaReports(9.5)
        every { enrollments.find(user.id, offering) } returns stored(9.5)

        sync(user, SyncScope.Period(period))

        verify(exactly = 0) { push.sendTo(any(), any()) }
    }

    test("pushes nothing when storing fails") {
        sigaReports(9.5)
        every { enrollments.find(user.id, offering) } returns stored(0.0)
        every { enrollments.save(any()) } throws IllegalStateException("database down")

        shouldThrow<IllegalStateException> { sync(user, SyncScope.Period(period)) }

        verify(exactly = 0) { push.sendTo(any(), any()) }
    }
})
