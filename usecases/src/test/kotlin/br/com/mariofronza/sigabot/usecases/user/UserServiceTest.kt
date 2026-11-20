package br.com.mariofronza.sigabot.usecases.user

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.user.AcademicAccountAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.credential.gateway.SecretCipher
import br.com.mariofronza.sigabot.usecases.institution.gateway.InstitutionInteractor
import br.com.mariofronza.sigabot.usecases.user.dto.CreateUserInput
import br.com.mariofronza.sigabot.usecases.user.dto.SigaCredentialRequest
import br.com.mariofronza.sigabot.usecases.user.gateway.AcademicAccountInteractor
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class UserServiceTest : FunSpec({

    val udesc = Institution(code = InstitutionCode.UDESC, name = "UDESC", gradeScaleMax = 10.0)
    val userInteractor: UserInteractor = mockk()
    val institutionInteractor: InstitutionInteractor = mockk()
    val academicAccountInteractor: AcademicAccountInteractor = mockk()
    val secretCipher: SecretCipher = mockk()
    val saved = slot<User>()
    val service = UserService(userInteractor, institutionInteractor, academicAccountInteractor, { "hashed-$it" }, secretCipher)

    fun signUp(cpf: String = "00000000000") = service.createUser(
        CreateUserInput(email = "student@test.com", password = "secret", sigaCredential = SigaCredentialRequest(cpf = cpf, password = "siga"))
    )

    beforeTest {
        every { userInteractor.findByEmail(any()) } returns null
        every { institutionInteractor.findByCode(InstitutionCode.UDESC) } returns udesc
        every { academicAccountInteractor.existsByLoginDigest(InstitutionCode.UDESC, any()) } returns false
        every { secretCipher.encrypt(any()) } returns "encrypted"
        every { secretCipher.digest(any()) } returns byteArrayOf(1)
        every { userInteractor.save(capture(saved)) } answers { saved.captured }
    }

    test("signs up every new user as STUDENT") {
        signUp()

        saved.captured.roles shouldBe setOf(Role.STUDENT)
    }

    test("answers with the id the domain gave the new user") {
        val output = signUp()

        output.id shouldBe saved.captured.id.value
    }

    test("rejects a sign-up whose CPF is already registered, whatever its formatting") {
        every { secretCipher.digest("00000000000") } returns byteArrayOf(7)
        every { academicAccountInteractor.existsByLoginDigest(InstitutionCode.UDESC, byteArrayOf(7)) } returns true

        val error = shouldThrow<AcademicAccountAlreadyRegisteredException> { signUp(cpf = "000.000.000-00") }

        error.message shouldBe "Siga credential already exists"
    }
})
