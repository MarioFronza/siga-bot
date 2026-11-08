package br.com.mariofronza.sigabot.usecases.notification

import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.misc.filter.UserDetailsImpl
import br.com.mariofronza.sigabot.usecases.notification.dto.NotificationInput
import br.com.mariofronza.sigabot.usecases.notification.dto.NotificationOutput
import br.com.mariofronza.sigabot.usecases.notification.gateway.NotificationInteractor
import br.com.mariofronza.sigabot.usecases.user.UserService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val notificationInteractor: NotificationInteractor,
    private val userService: UserService,
    private val socketNotification: PushNotification,
) {

    fun listNotifications(): List<NotificationOutput> {
        val user = getCurrentUser()
        return notificationInteractor.findByUser(user).map {
            NotificationOutput(
                description = it.description,
                entityId = it.entityId
            )
        }
    }

    fun createNotificationToAll(message: String) {
        socketNotification.notifyAllUsers(message)
    }

    fun createNotificationToUser(userId: String, notificationInput: NotificationInput): Notification {
        val user = userService.findById(userId.toLong())
        val notification = Notification(
            user = user,
            description = notificationInput.description,
            entityId = notificationInput.entityId
        )
        socketNotification.notifyUser(userId.toLong(), notification.description)
        return notificationInteractor.save(notification)
    }

    private fun getCurrentUser(): User {
        val currentUser = SecurityContextHolder.getContext().authentication!!.principal as UserDetailsImpl
        return currentUser.toUser()
    }
}
