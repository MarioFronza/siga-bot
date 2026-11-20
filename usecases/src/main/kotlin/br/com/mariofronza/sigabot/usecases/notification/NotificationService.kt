package br.com.mariofronza.sigabot.usecases.notification

import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.notification.dto.NotificationOutput
import br.com.mariofronza.sigabot.usecases.notification.gateway.NotificationInteractor
import br.com.mariofronza.sigabot.usecases.user.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class NotificationService(
    private val notificationInteractor: NotificationInteractor,
    private val userService: UserService,
    private val socketNotification: PushNotification,
) {

    fun listNotifications(user: User, pageable: Pageable): Page<NotificationOutput> =
        notificationInteractor.findByUser(user, pageable).map(NotificationOutput::from)

    fun createNotificationToAll(message: String) {
        socketNotification.notifyAllUsers(message)
    }

    fun createNotificationToUser(externalId: UUID, message: String): NotificationOutput {
        val user = userService.findByExternalId(externalId)
        socketNotification.notifyUser(externalId, message)
        return NotificationOutput.from(notificationInteractor.save(Notification(user = user, message = message)))
    }
}
