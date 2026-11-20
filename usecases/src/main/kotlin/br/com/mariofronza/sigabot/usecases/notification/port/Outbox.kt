package br.com.mariofronza.sigabot.usecases.notification.port

import br.com.mariofronza.sigabot.entity.user.UserId
import java.time.Duration
import java.time.Instant

interface Outbox {

    fun add(recipient: UserId, message: String)

    fun claimPending(now: Instant, limit: Int, lease: Duration, maxAttempts: Int): List<OutboxMessage>

    fun delivered(id: Long)

    fun failed(id: Long, retryAt: Instant)

    fun countPending(maxAttempts: Int): Long
}

data class OutboxMessage(val id: Long, val recipient: UserId, val message: String, val attempts: Int)
