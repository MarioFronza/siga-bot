package br.com.mariofronza.sigabot.usecases.notification.port

import br.com.mariofronza.sigabot.entity.user.UserId

interface Outbox {

    fun add(recipient: UserId, message: String)

    fun pending(limit: Int, maxAttempts: Int): List<OutboxMessage>

    fun delivered(id: Long)

    fun failed(id: Long)

    fun countPending(maxAttempts: Int): Long
}

data class OutboxMessage(val id: Long, val recipient: UserId, val message: String)
