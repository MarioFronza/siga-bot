package br.com.mariofronza.sigabot.usecases.identity

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.user.AcademicAccountAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.EmailAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.identity.port.InstitutionRepository
import br.com.mariofronza.sigabot.usecases.identity.port.SecretCipher
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class RegisterStudentTest : FunSpec({

    val users: UserRepository = mockk()
    val institutions: InstitutionRepository = mockk()
    val secretCipher: SecretCipher = mockk()
    val saved = slot<User>()
    val registerStudent = RegisterStudent(users, institutions, { "hashed-$it" }, secretCipher)

    fun register(cpf: String = "00000000000") =
        registerStudent(RegisterStudent.Command(email = "Student@Test.com", password = "secret", cpf = cpf, sigaPassword = "siga"))

    beforeTest {
        every { users.findByEmail(any()) } returns null
        every { users.existsByLoginDigest(InstitutionCode.UDESC, any()) } returns false
        every { users.save(capture(saved)) } answers { saved.captured }
        every { institutions.findByCode(InstitutionCode.UDESC) } returns Institution(InstitutionCode.UDESC, "UDESC", 10.0)
        every { secretCipher.encrypt(any()) } returns "encrypted"
        every { secretCipher.digest(any()) } returns byteArrayOf(1)
    }

    test("registers a student with a hashed password and an encrypted SIGA account") {
        val student = register()

        student shouldBe saved.captured
        student.roles shouldBe setOf(Role.STUDENT)
        student.email.value shouldBe "student@test.com"
        student.passwordHash shouldBe "hashed-secret"
        student.academicAccounts.single().passwordCiphertext shouldBe "encrypted"
    }

    test("rejects an email that is already registered") {
        every { users.findByEmail(any()) } returns saved.captured

        shouldThrow<EmailAlreadyRegisteredException> { register() }
    }

    test("rejects a CPF that is already registered, whatever its formatting") {
        every { secretCipher.digest("00000000000") } returns byteArrayOf(7)
        every { users.existsByLoginDigest(InstitutionCode.UDESC, byteArrayOf(7)) } returns true

        shouldThrow<AcademicAccountAlreadyRegisteredException> { register(cpf = "000.000.000-00") }
    }
})
