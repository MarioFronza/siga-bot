package br.com.mariofronza.sigabot.usecases.notification

interface PushNotification {

    fun notifyAllUsers(message: String)

    fun notifyUser(userId: Long, message: String)
}
