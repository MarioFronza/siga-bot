package br.com.mariofronza.sigabot.usecases.academic

import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import org.slf4j.LoggerFactory

class SyncAllStudents(
    private val users: UserRepository,
    private val syncStudentGrades: SyncStudentGrades,
) {

    operator fun invoke(scope: SyncScope) {
        val students = users.findAllActive()
        logger.info("Syncing {} for {} students", scope, students.size)
        val failures = students.count { !sync(it, scope) }
        logger.info("Sync finished, {} of {} students failed", failures, students.size)
    }

    private fun sync(student: User, scope: SyncScope): Boolean = try {
        syncStudentGrades(student, scope)
        true
    } catch (e: Exception) {
        logger.warn("Sync failed for user {}", student.id, e)
        false
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SyncAllStudents::class.java)
    }
}
