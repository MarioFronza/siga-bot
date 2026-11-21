package br.com.mariofronza.sigabot.entity

import br.com.mariofronza.sigabot.entity.catalog.SubjectOffering
import br.com.mariofronza.sigabot.entity.enrollment.Attendance
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.enrollment.GradePosted
import br.com.mariofronza.sigabot.entity.enrollment.Grade
import br.com.mariofronza.sigabot.entity.enrollment.SemesterOutcome
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.NoAcademicAccountException
import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.entity.user.UserId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class AggregatesTest : FunSpec({

    val student = UserId.generate()
    val offering = SubjectOffering(
        institution = InstitutionCode.UDESC,
        period = AcademicPeriod(2026, 1),
        course = "Engenharia de Software",
        classSection = "ESO-1",
        subject = "Banco de Dados",
        courseLoad = 72,
    )

    fun outcome(grade: Double?, absences: Int = 0) =
        SemesterOutcome(grade = grade?.let(::Grade), absences = absences, attendance = Attendance(100.0), result = "Aprovado")

    test("starts an enrollment without announcing the grade it was found with") {
        val recorded = Enrollment.record(student, offering, existing = null, outcome = outcome(8.0))

        recorded.enrollment.grade shouldBe Grade(8.0)
        recorded.events.shouldBeEmpty()
    }

    test("announces the first grade of a subject in progress") {
        val existing = Enrollment.record(student, offering, existing = null, outcome = outcome(null)).enrollment

        val recorded = Enrollment.record(student, offering, existing, outcome(9.5))

        recorded.events shouldContainExactly listOf(GradePosted(student, offering, Grade(9.5)))
    }

    test("does not announce a grade that was taken down") {
        val existing = Enrollment.record(student, offering, existing = null, outcome = outcome(9.5)).enrollment

        val recorded = Enrollment.record(student, offering, existing, outcome(null))

        recorded.enrollment.grade shouldBe null
        recorded.events.shouldBeEmpty()
    }

    test("announces a grade that changed") {
        val existing = Enrollment.record(student, offering, existing = null, outcome = outcome(0.0)).enrollment

        val recorded = Enrollment.record(student, offering, existing, outcome(9.5))

        recorded.enrollment.grade shouldBe Grade(9.5)
        recorded.events shouldContainExactly listOf(GradePosted(student, offering, Grade(9.5)))
    }

    test("updates absences without announcing an unchanged grade") {
        val existing = Enrollment.record(student, offering, existing = null, outcome = outcome(9.5)).enrollment

        val recorded = Enrollment.record(student, offering, existing, outcome(9.5, absences = 4))

        recorded.enrollment.absences shouldBe 4
        recorded.events.shouldBeEmpty()
    }

    test("replaces the academic account of the same institution") {
        val old = AcademicAccount(InstitutionCode.UDESC, "old", byteArrayOf(1), "old-password")
        val new = AcademicAccount(InstitutionCode.UDESC, "new", byteArrayOf(2), "new-password")

        val user = User.registerStudent(Email.of("ana@test.com"), "hash", old).replaceAccount(new)

        user.academicAccounts shouldBe listOf(new)
    }

    test("registers a student with the student role and one academic account") {
        val account = AcademicAccount(InstitutionCode.UDESC, "login", byteArrayOf(1), "password")

        val user = User.registerStudent(Email.of("ana@test.com"), "hash", account)

        user.roles shouldBe setOf(Role.STUDENT)
        user.isActive shouldBe true
        user.accountAt(InstitutionCode.UDESC) shouldBe account
    }

    test("refuses to use an institution the user has no account at") {
        val user = User.registerStudent(
            Email.of("ana@test.com"),
            "hash",
            AcademicAccount(InstitutionCode.UDESC, "login", byteArrayOf(1), "password"),
        )

        shouldThrow<NoAcademicAccountException> { user.accountAt(InstitutionCode("ufpr")) }
    }

    test("keeps the full name SIGA reports and derives the first name") {
        val user = User.registerStudent(
            Email.of("ana@test.com"),
            "hash",
            AcademicAccount(InstitutionCode.UDESC, "login", byteArrayOf(1), "password"),
        ).rename("Ana Souza")

        user.fullName shouldBe "Ana Souza"
        user.username shouldBe "Ana"
    }
})
