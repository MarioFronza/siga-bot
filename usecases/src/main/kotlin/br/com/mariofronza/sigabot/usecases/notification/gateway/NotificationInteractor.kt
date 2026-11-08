package br.com.mariofronza.sigabot.usecases.notification.gateway

import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.user.User

interface NotificationInteractor {

    fun findByUser(user: User): List<Notification>

    fun save(notification: Notification): Notification
}
