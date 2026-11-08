package br.com.mariofronza.sigabot.persistence.notification.gateway

import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.misc.toJPA
import br.com.mariofronza.sigabot.persistence.notification.repository.NotificationRepository
import br.com.mariofronza.sigabot.usecases.notification.gateway.NotificationInteractor
import org.springframework.stereotype.Component

@Component
class NotificationRepositoryInteractor(
    private val notificationRepository: NotificationRepository
) : NotificationInteractor {

    override fun findByUser(user: User): List<Notification> {
        return notificationRepository.findByUser(user.toJPA()).map {
            it.toEntity()
        }
    }

    override fun save(notification: Notification): Notification {
        return notificationRepository.save(notification.toJPA()).toEntity()
    }
}
