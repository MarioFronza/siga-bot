package br.com.mariofronza.sigabot.usecases.notification.dto

import br.com.mariofronza.sigabot.entity.notification.Notification
import java.time.Instant

data class NotificationOutput(
    val id: Long,
    val message: String,
    val createdAt: Instant,
) {
    companion object {
        fun from(notification: Notification) = NotificationOutput(
            id = notification.id!!,
            message = notification.message,
            createdAt = notification.createdAt!!,
        )
    }
}
