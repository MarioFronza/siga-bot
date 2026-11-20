package br.com.mariofronza.sigabot.usecases.identity

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class GrantAdminsTest : FunSpec({

    test("grants ADMIN to the listed users that exist and are not admins yet") {
        val ana = User.registerStudent(Email.of("ana@test.com"), "hash", AcademicAccount(InstitutionCode.UDESC, "l", byteArrayOf(1), "p"))
        val users: UserRepository = mockk(relaxed = true)
        every { users.findByEmail(Email.of("ana@test.com")) } returns ana
        every { users.findByEmail(Email.of("nobody@test.com")) } returns null

        GrantAdmins(users, setOf(Email.of("ana@test.com"), Email.of("nobody@test.com")))() shouldBe 1

        verify { users.save(ana.grant(Role.ADMIN)) }
    }
})
