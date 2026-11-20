package br.com.mariofronza.sigabot.usecases.notification

import java.util.UUID

interface PushNotification {

    fun notifyAllUsers(message: String)

    fun notifyUser(externalId: UUID, message: String)
}
