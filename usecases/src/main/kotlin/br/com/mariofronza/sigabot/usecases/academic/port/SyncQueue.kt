package br.com.mariofronza.sigabot.usecases.academic.port

import br.com.mariofronza.sigabot.entity.user.UserId
import java.time.Duration
import java.time.Instant

interface SyncQueue {

    fun enqueue(userId: UserId, runAt: Instant)

    fun request(userId: UserId, scope: SyncScope, runAt: Instant)

    fun claimDue(now: Instant, limit: Int, lease: Duration): List<SyncJob>

    fun completed(userId: UserId, syncedAt: Instant, nextRunAt: Instant, fulfilled: SyncScope?)

    fun failed(userId: UserId, error: String, retryAt: Instant)

    fun countDue(now: Instant): Long

    fun statusOf(userId: UserId, now: Instant): SyncStatus?
}

data class SyncJob(val userId: UserId, val attempts: Int, val requested: SyncScope? = null)

data class SyncStatus(
    val lastSyncedAt: Instant?,
    val nextRunAt: Instant,
    val running: Boolean,
    val pending: Boolean,
    val attempts: Int,
    val lastError: String?,
)
