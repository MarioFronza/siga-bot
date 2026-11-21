package br.com.mariofronza.sigabot.usecases.academic

import br.com.mariofronza.sigabot.usecases.academic.port.SyncJob
import br.com.mariofronza.sigabot.usecases.academic.port.SyncObserver
import br.com.mariofronza.sigabot.usecases.academic.port.SyncQueue
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore

class RunDueSyncs(
    private val queue: SyncQueue,
    private val users: UserRepository,
    private val syncStudentGrades: SyncStudentGrades,
    private val observer: SyncObserver,
    private val clock: Clock,
    private val settings: Settings = Settings(),
) : AutoCloseable {

    data class Settings(
        val interval: Duration = Duration.ofHours(1),
        val concurrency: Int = 8,
        val lease: Duration = Duration.ofMinutes(10),
        val firstRetry: Duration = Duration.ofMinutes(5),
        val maxRetry: Duration = Duration.ofHours(6),
    )

    private val slots = Semaphore(settings.concurrency)
    private val executor = Executors.newVirtualThreadPerTaskExecutor()

    operator fun invoke(scope: SyncScope): Int {
        val free = slots.drainPermits()
        if (free == 0) return 0
        val jobs = try {
            queue.claimDue(clock.instant(), free, settings.lease)
        } catch (e: Exception) {
            slots.release(free)
            throw e
        }
        slots.release(free - jobs.size)
        jobs.forEach { job ->
            executor.submit {
                try {
                    run(job, scope)
                } finally {
                    slots.release()
                }
            }
        }
        return jobs.size
    }

    private fun run(job: SyncJob, scope: SyncScope) {
        val started = clock.instant()
        try {
            val student = checkNotNull(users.findById(job.userId)) { "User ${job.userId} is gone" }
            syncStudentGrades(student, job.requested ?: scope)
            observer.synced(Duration.between(started, clock.instant()))
            val finished = clock.instant()
            queue.completed(job.userId, finished, finished.plus(settings.interval), job.requested)
        } catch (e: Exception) {
            logger.warn("Sync failed for user {} (attempt {})", job.userId, job.attempts + 1, e)
            observer.failed(e)
            queue.failed(job.userId, e.message ?: e.javaClass.simpleName, clock.instant().plus(retryDelay(job.attempts + 1)))
        }
    }

    override fun close() = executor.close()

    internal fun retryDelay(attempt: Int): Duration {
        val doublings = (attempt - 1).coerceIn(0, 20)
        return settings.firstRetry.multipliedBy(1L shl doublings).coerceAtMost(settings.maxRetry)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RunDueSyncs::class.java)
    }
}
