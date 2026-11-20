package br.com.mariofronza.sigabot.usecases.user

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.credential.gateway.SecretCipher
import br.com.mariofronza.sigabot.usecases.user.dto.CreateUserInput
import br.com.mariofronza.sigabot.usecases.user.dto.SigaCredentialRequest
import br.com.mariofronza.sigabot.usecases.institution.gateway.InstitutionInteractor
import br.com.mariofronza.sigabot.usecases.user.gateway.AcademicAccountInteractor
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.util.UUID

class UserServiceTest : FunSpec({

    val udesc = Institution(id = 1, code = "udesc", name = "UDESC", gradeScaleMax = 10.0)

    test("signs up every new user as STUDENT") {
        val userInteractor: UserInteractor = mockk()
        val roleService: RoleService = mockk()
        val institutionInteractor: InstitutionInteractor = mockk()
        val academicAccountInteractor: AcademicAccountInteractor = mockk()
        val secretCipher: SecretCipher = mockk()
        val savedUser = slot<User>()

        every { userInteractor.findByEmail(any()) } returns null
        every { institutionInteractor.findByCode("udesc") } returns udesc
        every { academicAccountInteractor.existsByLoginDigest(udesc, any()) } returns false
        every { roleService.findByName(any()) } answers { Role(id = 1, name = firstArg()) }
        every { secretCipher.encrypt(any()) } returns "encrypted"
        every { secretCipher.digest(any()) } returns byteArrayOf(1)
        every { userInteractor.save(capture(savedUser)) } answers { savedUser.captured.copy(id = 1, externalId = UUID.randomUUID()) }

        UserService(userInteractor, roleService, institutionInteractor, academicAccountInteractor, { "hashed-$it" }, secretCipher)
            .createUser(
                CreateUserInput(
                    email = "attacker@test.com",
                    password = "secret",
                    sigaCredential = SigaCredentialRequest(cpf = "00000000000", password = "siga")
                )
            )

        savedUser.captured.roles.map { it.name } shouldBe listOf("STUDENT")
    }

    test("answers with the public external id instead of the internal one") {
        val externalId = UUID.fromString("01a1f973-13ad-7a5c-80df-bf421ada5df1")
        val userInteractor: UserInteractor = mockk()
        val roleService: RoleService = mockk()
        val institutionInteractor: InstitutionInteractor = mockk()
        val academicAccountInteractor: AcademicAccountInteractor = mockk()
        val secretCipher: SecretCipher = mockk()

        every { userInteractor.findByEmail(any()) } returns null
        every { institutionInteractor.findByCode("udesc") } returns udesc
        every { academicAccountInteractor.existsByLoginDigest(udesc, any()) } returns false
        every { roleService.findByName(any()) } answers { Role(id = 1, name = firstArg()) }
        every { secretCipher.encrypt(any()) } returns "encrypted"
        every { secretCipher.digest(any()) } returns byteArrayOf(1)
        every { userInteractor.save(any()) } answers { firstArg<User>().copy(id = 1, externalId = externalId) }

        val output = UserService(userInteractor, roleService, institutionInteractor, academicAccountInteractor, { "hashed-$it" }, secretCipher)
            .createUser(
                CreateUserInput(
                    email = "student@test.com",
                    password = "secret",
                    sigaCredential = SigaCredentialRequest(cpf = "00000000000", password = "siga")
                )
            )

        output.id shouldBe externalId
    }

    test("rejects a sign-up whose CPF is already registered") {
        val userInteractor: UserInteractor = mockk()
        val institutionInteractor: InstitutionInteractor = mockk()
        val academicAccountInteractor: AcademicAccountInteractor = mockk()
        val secretCipher: SecretCipher = mockk()

        every { userInteractor.findByEmail(any()) } returns null
        every { secretCipher.digest("00000000000") } returns byteArrayOf(7)
        every { institutionInteractor.findByCode("udesc") } returns udesc
        every { academicAccountInteractor.existsByLoginDigest(udesc, byteArrayOf(7)) } returns true

        val error = shouldThrow<AcademicAccountAlreadyRegisteredException> {
            UserService(userInteractor, mockk(), institutionInteractor, academicAccountInteractor, { "hashed-$it" }, secretCipher)
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
