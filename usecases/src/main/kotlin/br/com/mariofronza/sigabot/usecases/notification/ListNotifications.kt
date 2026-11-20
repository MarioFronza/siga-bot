package br.com.mariofronza.sigabot.usecases.notification

import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.usecases.notification.port.NotificationRepository
import br.com.mariofronza.sigabot.usecases.shared.Page
import br.com.mariofronza.sigabot.usecases.shared.PageRequest

class ListNotifications(private val notifications: NotificationRepository) {

    operator fun invoke(recipient: UserId, request: PageRequest): Page<Notification> =
        notifications.findByRecipient(recipient, request)
}
