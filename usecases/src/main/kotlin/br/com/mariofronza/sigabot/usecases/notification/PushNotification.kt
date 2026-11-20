package br.com.mariofronza.sigabot.usecases.notification

import br.com.mariofronza.sigabot.entity.user.UserId

interface PushNotification {

    fun notifyAllUsers(message: String)

    fun notifyUser(userId: UserId, message: String)
}
