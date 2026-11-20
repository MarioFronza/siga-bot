package br.com.mariofronza.sigabot.usecases.user

import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.misc.cryptograpy.CryptographyService
import br.com.mariofronza.sigabot.usecases.user.dto.CreateUserInput
import br.com.mariofronza.sigabot.usecases.user.dto.SigaCredentialRequest
import br.com.mariofronza.sigabot.usecases.user.gateway.SigaCredentialInteractor
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import io.kotest.assertions.throwables.shouldThrow
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
        every { sigaCredentialInteractor.existsByCpfDigest(any()) } returns false
        every { roleService.findByName(any()) } answers { Role(id = 1, name = firstArg()) }
        every { cryptographyService.encrypt(any()) } returns "encrypted"
        every { cryptographyService.digest(any()) } returns byteArrayOf(1)
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

    test("rejects a sign-up whose CPF is already registered") {
        val userInteractor: UserInteractor = mockk()
        val sigaCredentialInteractor: SigaCredentialInteractor = mockk()
        val cryptographyService: CryptographyService = mockk()

        every { userInteractor.findByEmail(any()) } returns null
        every { cryptographyService.digest("00000000000") } returns byteArrayOf(7)
        every { sigaCredentialInteractor.existsByCpfDigest(byteArrayOf(7)) } returns true

        val error = shouldThrow<Exception> {
            UserService(userInteractor, mockk(), sigaCredentialInteractor, BCryptPasswordEncoder(), cryptographyService)
                .createUser(
                    CreateUserInput(
                        email = "student@test.com",
                        password = "secret",
                        sigaCredential = SigaCredentialRequest(cpf = "000.000.000-00", password = "siga")
                    )
                )
        }

        error.message shouldBe "Siga credential already exists"
    }
})
