package br.com.mariofronza.sigabot.usecases.identity

import br.com.mariofronza.sigabot.entity.user.Email
import java.time.ZoneOffset
import java.time.Instant
import java.time.Clock
import io.mockk.verify
import br.com.mariofronza.sigabot.usecases.shared.TransactionRunner
import br.com.mariofronza.sigabot.usecases.academic.port.SyncQueue
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
    val queue: SyncQueue = mockk(relaxed = true)
    val now = Instant.parse("2026-11-20T12:00:00Z")
    val transaction = object : TransactionRunner {
        override fun <T> inTransaction(block: () -> T): T = block()
    }
    val saved = slot<User>()
    val registerStudent = RegisterStudent(users, institutions, { "hashed-$it" }, secretCipher, queue, transaction, Clock.fixed(now, ZoneOffset.UTC))

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

    test("grants ADMIN at sign-up only to an email the operator listed") {
        val withAdmins = RegisterStudent(
            users, institutions, { "hashed-$it" }, secretCipher, queue, transaction, Clock.fixed(now, ZoneOffset.UTC),
            adminEmails = setOf(Email.of("student@test.com")),
        )

        withAdmins(RegisterStudent.Command("student@test.com", "secret", "00000000000", "siga")).roles shouldBe setOf(Role.STUDENT, Role.ADMIN)
    }

    test("queues the first sync of a new student right away") {
        val student = register()

        verify { queue.enqueue(student.id, now) }
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
