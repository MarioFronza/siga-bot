package br.com.mariofronza.sigabot.usecases.academic

import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.enrollment.GradePosted
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.academic.port.AcademicRecord
import br.com.mariofronza.sigabot.usecases.academic.port.AcademicSystem
import br.com.mariofronza.sigabot.usecases.academic.port.EnrollmentRepository
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import br.com.mariofronza.sigabot.usecases.identity.port.SecretCipher
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import br.com.mariofronza.sigabot.usecases.notification.port.Outbox
import br.com.mariofronza.sigabot.usecases.shared.TransactionRunner
import org.slf4j.LoggerFactory

class SyncStudentGrades(
    private val users: UserRepository,
    private val enrollments: EnrollmentRepository,
    private val academicSystems: List<AcademicSystem>,
    private val secretCipher: SecretCipher,
    private val outbox: Outbox,
    private val transaction: TransactionRunner,
) {

    operator fun invoke(user: User, scope: SyncScope): List<AcademicPeriod> =
        user.academicAccounts.flatMap { sync(user, it, scope) }.distinct()

    private fun sync(user: User, account: AcademicAccount, scope: SyncScope): List<AcademicPeriod> {
        logger.debug("Syncing user {} at {}", user.id, account.institution)
        val record = systemFor(account).fetch(credentialsOf(account), scope)
        val student = user.rename(record.studentName)

        transaction.inTransaction { store(user, student, record).forEach { announce(student, it) } }
        return record.periods
    }

    private fun store(stored: User, student: User, record: AcademicRecord): List<GradePosted> {
        if (student != stored) users.save(student)
        val existing = record.results.map { it.offering.period }.distinct()
            .flatMap { enrollments.findAll(student.id, it) }
            .associateBy { it.offering.key }
        return record.results.flatMap { result ->
            val previous = existing[result.offering.key]
            val recorded = Enrollment.record(student.id, result.offering, previous, result.outcome)
            if (recorded.enrollment != previous) enrollments.save(recorded.enrollment)
            recorded.events
        }
    }

    private fun announce(student: User, event: GradePosted) {
        logger.info("New grade for user {} in {}", student.id, event.offering.subject)
        outbox.add(student.id, "${student.username}, você possui uma nota nova da matéria ${event.offering.subject}")
    }

    private fun systemFor(account: AcademicAccount) =
        checkNotNull(academicSystems.firstOrNull { it.institution == account.institution }) {
            "No academic system for ${account.institution}"
        }

    private fun credentialsOf(account: AcademicAccount) = AcademicSystem.Credentials(
        login = secretCipher.decrypt(account.loginCiphertext),
        password = secretCipher.decrypt(account.passwordCiphertext),
    )

    companion object {
        private val logger = LoggerFactory.getLogger(SyncStudentGrades::class.java)
    }
}
