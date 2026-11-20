package br.com.mariofronza.sigabot.usecases.identity

import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.AcademicAccountAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.Cpf
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.EmailAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.identity.port.InstitutionRepository
import br.com.mariofronza.sigabot.usecases.identity.port.PasswordHasher
import br.com.mariofronza.sigabot.usecases.identity.port.SecretCipher
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import br.com.mariofronza.sigabot.usecases.academic.port.SyncQueue
import br.com.mariofronza.sigabot.usecases.shared.TransactionRunner
import java.time.Clock

class RegisterStudent(
    private val users: UserRepository,
    private val institutions: InstitutionRepository,
    private val passwordHasher: PasswordHasher,
    private val secretCipher: SecretCipher,
    private val syncQueue: SyncQueue,
    private val transaction: TransactionRunner,
    private val clock: Clock,
    private val adminEmails: Set<Email> = emptySet(),
) {

    data class Command(val email: String, val password: String, val cpf: String, val sigaPassword: String)

    operator fun invoke(command: Command): User {
        val email = Email.of(command.email)
        val loginDigest = secretCipher.digest(Cpf.of(command.cpf).value)
        val institution = InstitutionCode.UDESC

        if (users.findByEmail(email) != null) throw EmailAlreadyRegisteredException()
        checkNotNull(institutions.findByCode(institution)) { "Institution $institution is not seeded" }
        if (users.existsByLoginDigest(institution, loginDigest)) throw AcademicAccountAlreadyRegisteredException()

        val account = AcademicAccount(
            institution = institution,
            loginCiphertext = secretCipher.encrypt(command.cpf),
            loginDigest = loginDigest,
            passwordCiphertext = secretCipher.encrypt(command.sigaPassword),
        )
        val student = User.registerStudent(email, passwordHasher.hash(command.password), account)
            .let { if (email in adminEmails) it.grant(Role.ADMIN) else it }
        return transaction.inTransaction {
            users.save(student).also { syncQueue.enqueue(it.id, clock.instant()) }
        }
    }
}
