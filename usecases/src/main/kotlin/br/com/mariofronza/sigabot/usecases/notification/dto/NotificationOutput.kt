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
            id = checkNotNull(notification.id) { "Notification has no id until it is saved" },
            message = notification.message,
            createdAt = checkNotNull(notification.createdAt) { "Notification has no creation time until it is saved" },
        )
    }
}
