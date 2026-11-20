package br.com.mariofronza.sigabot.usecases.notification

import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.usecases.notification.dto.NotificationOutput
import br.com.mariofronza.sigabot.usecases.notification.gateway.NotificationInteractor
import br.com.mariofronza.sigabot.usecases.user.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import br.com.mariofronza.sigabot.entity.user.UserId

@Service
class NotificationService(
    private val notificationInteractor: NotificationInteractor,
    private val userService: UserService,
    private val socketNotification: PushNotification,
) {

    fun listNotifications(recipient: UserId, pageable: Pageable): Page<NotificationOutput> =
        notificationInteractor.findByRecipient(recipient, pageable).map(NotificationOutput::from)

    fun createNotificationToAll(message: String) {
        socketNotification.notifyAllUsers(message)
    }

    fun createNotificationToUser(id: UserId, message: String): NotificationOutput {
        val recipient = userService.findById(id).id
        socketNotification.notifyUser(recipient, message)
        return NotificationOutput.from(notificationInteractor.save(Notification(recipient = recipient, message = message)))
    }
}
