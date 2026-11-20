package br.com.mariofronza.sigabot.persistence.notification.gateway

import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.notification.entity.NotificationJPA
import br.com.mariofronza.sigabot.persistence.notification.repository.NotificationRepository
import br.com.mariofronza.sigabot.persistence.user.repository.UserRepository
import br.com.mariofronza.sigabot.usecases.notification.gateway.NotificationInteractor
import org.springframework.stereotype.Component

@Component
class NotificationRepositoryInteractor(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
) : NotificationInteractor {

    override fun findByUser(user: User): List<Notification> =
        notificationRepository.findByUserIdOrderByCreatedAtDesc(user.id!!).map { it.toEntity(user) }

    override fun save(notification: Notification): Notification {
        val saved = notificationRepository.save(
            NotificationJPA(user = userRepository.getReferenceById(notification.user.id!!), message = notification.message)
        )
        return notification.copy(id = saved.id)
    }
}
