package br.com.mariofronza.sigabot.usecases.notification

import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.notification.NotificationKind
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.entity.user.UserNotFoundException
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import br.com.mariofronza.sigabot.usecases.notification.port.NotificationRepository
import br.com.mariofronza.sigabot.usecases.notification.port.Outbox
import br.com.mariofronza.sigabot.usecases.shared.TransactionRunner

class NotifyStudent(
    private val users: UserRepository,
    private val notifications: NotificationRepository,
    private val outbox: Outbox,
    private val transaction: TransactionRunner,
) {

    data class Sent(val notification: Notification, val recipient: User)

    operator fun invoke(recipient: UserId, message: String): Sent {
        val student = users.findById(recipient) ?: throw UserNotFoundException(recipient)
        val notification = transaction.inTransaction {
            outbox.add(student.id, message)
            notifications.save(Notification(recipient = student.id, message = message, kind = NotificationKind.ANNOUNCEMENT))
        }
        return Sent(notification, student)
    }
}
