package br.com.mariofronza.sigabot.usecases.notification.port

import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.usecases.shared.Page
import br.com.mariofronza.sigabot.usecases.shared.PageRequest

interface NotificationRepository {

    fun findByRecipient(recipient: UserId, request: PageRequest): Page<Notification>

    fun save(notification: Notification): Notification
}
