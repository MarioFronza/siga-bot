package br.com.mariofronza.sigabot.usecases.misc.filter

import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.SigaCredential
import br.com.mariofronza.sigabot.entity.user.User
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class UserDetailsImplTest : FunSpec({

    test("grants one authority per user role") {
        val user = User(
            isActive = true,
            email = "student@test.com",
            passwordHash = "hash",
            roles = listOf(Role(name = "STUDENT")),
            sigaCredential = SigaCredential(cpfHash = "cpf", passwordHash = "password")
        )

        UserDetailsImpl(user).authorities.map { it.authority } shouldBe listOf("STUDENT")
    }
})
