package br.com.mariofronza.sigabot.usecases.academic

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class SyncAllStudentsTest : FunSpec({

    fun student(name: String) =
        User.registerStudent(Email.of("$name@test.com"), "hash", AcademicAccount(InstitutionCode.UDESC, name, byteArrayOf(1), "password"))

    test("keeps syncing the remaining students after one fails") {
        val (first, failing, last) = listOf(student("first"), student("failing"), student("last"))
        val period = AcademicPeriod(2026, 1)
        val users: UserRepository = mockk()
        val syncStudentGrades: SyncStudentGrades = mockk(relaxed = true)
        every { users.findAllActive() } returns listOf(first, failing, last)
        every { syncStudentGrades(failing, any()) } throws IllegalStateException("SIGA rejected the login")

        SyncAllStudents(users, syncStudentGrades)(period)

        verify { syncStudentGrades(last, SyncScope.Period(period)) }
    }
})
