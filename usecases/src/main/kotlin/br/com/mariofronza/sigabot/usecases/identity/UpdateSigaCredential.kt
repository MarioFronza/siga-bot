package br.com.mariofronza.sigabot.usecases.identity

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.AcademicAccountAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.Cpf
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.academic.port.SyncQueue
import br.com.mariofronza.sigabot.usecases.identity.port.SecretCipher
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import br.com.mariofronza.sigabot.usecases.shared.TransactionRunner
import java.time.Clock

class UpdateSigaCredential(
    private val users: UserRepository,
    private val secretCipher: SecretCipher,
    private val syncQueue: SyncQueue,
    private val transaction: TransactionRunner,
    private val clock: Clock,
) {

    data class Command(val cpf: String, val password: String)

    operator fun invoke(student: User, command: Command) {
        val institution = InstitutionCode.UDESC
        val loginDigest = secretCipher.digest(Cpf.of(command.cpf).value)
        val ownDigest = student.accountAt(institution).loginDigest
        if (!loginDigest.contentEquals(ownDigest) && users.existsByLoginDigest(institution, loginDigest)) {
            throw AcademicAccountAlreadyRegisteredException()
        }

        val account = AcademicAccount(
            institution = institution,
            loginCiphertext = secretCipher.encrypt(command.cpf),
            loginDigest = loginDigest,
            passwordCiphertext = secretCipher.encrypt(command.password),
        )
        transaction.inTransaction {
            users.save(student.replaceAccount(account))
            syncQueue.enqueue(student.id, clock.instant())
        }
    }
}
