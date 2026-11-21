package br.com.mariofronza.sigabot.usecases.identity

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.AcademicAccountAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.academic.port.SyncQueue
import br.com.mariofronza.sigabot.usecases.identity.port.SecretCipher
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import br.com.mariofronza.sigabot.usecases.shared.TransactionRunner
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class UpdateSigaCredentialTest : FunSpec({

    val users: UserRepository = mockk(relaxed = true)
    val cipher: SecretCipher = mockk()
    val queue: SyncQueue = mockk(relaxed = true)
    val now = Instant.parse("2026-11-20T12:00:00Z")
    var insideTransaction = false
    val transaction = object : TransactionRunner {
        override fun <T> inTransaction(block: () -> T): T {
            insideTransaction = true
            return block().also { insideTransaction = false }
        }
    }
    val update = UpdateSigaCredential(users, cipher, queue, transaction, Clock.fixed(now, ZoneOffset.UTC))
    val student = User.registerStudent(Email.of("ana@test.com"), "hash", AcademicAccount(InstitutionCode.UDESC, "old-login", byteArrayOf(1), "old-password"))

    beforeTest {
        clearMocks(users, queue, answers = false)
        every { cipher.encrypt(any()) } answers { "encrypted-${firstArg<String>()}" }
        every { cipher.digest("11111111111") } returns byteArrayOf(1)
        every { cipher.digest("22222222222") } returns byteArrayOf(2)
        every { users.existsByLoginDigest(InstitutionCode.UDESC, any()) } returns true
    }

    test("replaces the SIGA password and syncs right away") {
        val saved = slot<User>()
        var queuedInsideTransaction = false
        every { users.save(capture(saved)) } answers { saved.captured }
        every { queue.enqueue(student.id, now) } answers { queuedInsideTransaction = insideTransaction }

        update(student, UpdateSigaCredential.Command(cpf = "111.111.111-11", password = "new-password"))

        val account = saved.captured.academicAccounts.single()
        account.passwordCiphertext shouldBe "encrypted-new-password"
        account.loginCiphertext shouldBe "encrypted-111.111.111-11"
        queuedInsideTransaction shouldBe true
    }

    test("refuses a CPF another student already registered") {
        shouldThrow<AcademicAccountAlreadyRegisteredException> {
            update(student, UpdateSigaCredential.Command(cpf = "22222222222", password = "new-password"))
        }

        verify(exactly = 0) { users.save(any()) }
    }

    test("moves the account to a CPF nobody registered") {
        every { users.existsByLoginDigest(InstitutionCode.UDESC, byteArrayOf(2)) } returns false
        val saved = slot<User>()
        every { users.save(capture(saved)) } answers { saved.captured }

        update(student, UpdateSigaCredential.Command(cpf = "22222222222", password = "new-password"))

        saved.captured.academicAccounts.single().loginDigest shouldBe byteArrayOf(2)
    }
})
