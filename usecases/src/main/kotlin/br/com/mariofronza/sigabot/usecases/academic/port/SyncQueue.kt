package br.com.mariofronza.sigabot.usecases.academic.port

import br.com.mariofronza.sigabot.entity.user.UserId
import java.time.Duration
import java.time.Instant

interface SyncQueue {

    fun enqueue(userId: UserId, runAt: Instant)

    fun claimDue(now: Instant, limit: Int, lease: Duration): List<SyncJob>

    fun completed(userId: UserId, nextRunAt: Instant)

    fun failed(userId: UserId, error: String, retryAt: Instant)

    fun countDue(now: Instant): Long
}

data class SyncJob(val userId: UserId, val attempts: Int)
