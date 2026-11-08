package br.com.mariofronza.sigabot.usecases.user

import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.misc.cryptograpy.CryptographyService
import br.com.mariofronza.sigabot.usecases.user.dto.CreateUserInput
import br.com.mariofronza.sigabot.usecases.user.dto.SigaCredentialRequest
import br.com.mariofronza.sigabot.usecases.user.gateway.SigaCredentialInteractor
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class UserServiceTest : FunSpec({

    test("signs up every new user as STUDENT") {
        val userInteractor: UserInteractor = mockk()
        val roleService: RoleService = mockk()
        val sigaCredentialInteractor: SigaCredentialInteractor = mockk()
        val cryptographyService: CryptographyService = mockk()
        val savedUser = slot<User>()

        every { userInteractor.findByEmail(any()) } returns null
        every { sigaCredentialInteractor.findByCpf(any()) } returns null
        every { roleService.findByName(any()) } answers { Role(name = firstArg()) }
        every { cryptographyService.encrypt(any()) } returns "encrypted"
        every { userInteractor.save(capture(savedUser)) } answers { savedUser.captured.copy(id = 1) }

        UserService(userInteractor, roleService, sigaCredentialInteractor, BCryptPasswordEncoder(), cryptographyService)
            .createUser(
                CreateUserInput(
                    email = "attacker@test.com",
                    password = "secret",
                    sigaCredential = SigaCredentialRequest(cpf = "00000000000", password = "siga")
                )
            )

        savedUser.captured.roles.map { it.name } shouldBe listOf("STUDENT")
    }
})
