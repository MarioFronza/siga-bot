package br.com.mariofronza.sigabot.usecases.retrieve

import br.com.mariofronza.sigabot.entity.catalog.SubjectOffering
import br.com.mariofronza.sigabot.entity.enrollment.Attendance
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.enrollment.Grade
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.credential.gateway.SecretCipher
import br.com.mariofronza.sigabot.usecases.extraction.semester.SemesterResultsExtraction
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SemesterResultOutput
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SemesterResultPeriod
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SemesterResultsExtractionOutput
import br.com.mariofronza.sigabot.usecases.notification.PushNotification
import br.com.mariofronza.sigabot.usecases.retrieve.dto.RetrieveInput
import br.com.mariofronza.sigabot.usecases.siga.enrollment.gateway.EnrollmentInteractor
import br.com.mariofronza.sigabot.usecases.siga.term.dto.AcademicTermOutput
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class RetrieveServiceTest : FunSpec({

    val users: UserInteractor = mockk(relaxed = true)
    val enrollments: EnrollmentInteractor = mockk(relaxed = true)
    val extraction: SemesterResultsExtraction = mockk()
    val push: PushNotification = mockk(relaxed = true)
    val cipher: SecretCipher = mockk()
    val service = RetrieveService(users, enrollments, extraction, push, cipher)

    val user = User.registerStudent(Email.of("ana@test.com"), "hash", AcademicAccount(InstitutionCode.UDESC, "login", byteArrayOf(1), "password"))
    val offering = SubjectOffering(InstitutionCode.UDESC, AcademicPeriod(2026, 1), "Engenharia de Software", "ESO-1", "Banco de Dados", 72)

    fun sigaReports(grade: Double) {
        every { extraction.extract(any(), any()) } returns SemesterResultsExtractionOutput(
            studentName = "Ana Souza",
            course = "Engenharia de Software",
            semesterResultsPeriod = listOf(
                SemesterResultPeriod(
                    period = "2026/1",
                    semesterResults = listOf(SemesterResultOutput("Banco de Dados", "ESO-1", grade, 72, 0, 100.0, "Aprovado")),
                )
            ),
        )
    }

    fun stored(grade: Double) = Enrollment(user.id, offering, Grade(grade), 0, Attendance(100.0), "Aprovado")

    beforeTest {
        clearMocks(users, enrollments, push, answers = false)
        every { cipher.decrypt(any()) } returns "plain"
    }

    test("stores what SIGA reports and answers the synced periods") {
        sigaReports(8.0)
        every { enrollments.find(user.id, offering) } returns null
        val saved = slot<Enrollment>()
        every { enrollments.save(capture(saved)) } returns Unit

        service.retrieveForUser(RetrieveInput(2026, 1), user) shouldBe listOf(AcademicTermOutput(2026, 1))

        saved.captured shouldBe stored(8.0)
        verify { users.save(user.rename("Ana Souza")) }
    }

    test("pushes a notification when a stored grade changes") {
        sigaReports(9.5)
        every { enrollments.find(user.id, offering) } returns stored(0.0)

        service.retrieveForUser(RetrieveInput(2026, 1), user)

        verify { push.notifyUser(user.id, "Ana, você possui uma nota nova da matéria Banco de Dados") }
    }

    test("stays quiet when the grade did not change") {
        sigaReports(9.5)
        every { enrollments.find(user.id, offering) } returns stored(9.5)

        service.retrieveForUser(RetrieveInput(2026, 1), user)

        verify(exactly = 0) { push.notifyUser(any(), any()) }
    }
})
