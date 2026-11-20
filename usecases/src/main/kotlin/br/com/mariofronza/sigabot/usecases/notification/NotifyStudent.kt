package br.com.mariofronza.sigabot.usecases.notification

import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.entity.user.UserNotFoundException
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import br.com.mariofronza.sigabot.usecases.notification.port.NotificationRepository
import br.com.mariofronza.sigabot.usecases.notification.port.PushSender

class NotifyStudent(
    private val users: UserRepository,
    private val notifications: NotificationRepository,
    private val pushSender: PushSender,
) {

    operator fun invoke(recipient: UserId, message: String): Notification {
        val student = users.findById(recipient) ?: throw UserNotFoundException(recipient)
        val notification = notifications.save(Notification(recipient = student.id, message = message))
        pushSender.sendTo(student.id, message)
        return notification
    }
}
