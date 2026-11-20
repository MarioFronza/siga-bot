package br.com.mariofronza.sigabot.usecases.notification.gateway

import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.user.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface NotificationInteractor {

    fun findByRecipient(recipient: UserId, pageable: Pageable): Page<Notification>

    fun save(notification: Notification): Notification
}
