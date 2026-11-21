package br.com.mariofronza.sigabot.usecases.notification

import br.com.mariofronza.sigabot.entity.notification.NotificationKind
import br.com.mariofronza.sigabot.usecases.notification.port.NotificationRepository
import br.com.mariofronza.sigabot.usecases.notification.port.PushSender

class BroadcastNotification(
    private val notifications: NotificationRepository,
    private val pushSender: PushSender,
) {

    operator fun invoke(message: String) {
        notifications.saveForEveryActiveUser(message, NotificationKind.ANNOUNCEMENT)
        pushSender.sendToAll(message)
    }
}
