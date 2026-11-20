package br.com.mariofronza.sigabot.usecases.notification

import br.com.mariofronza.sigabot.usecases.notification.port.PushSender

class BroadcastNotification(private val pushSender: PushSender) {

    operator fun invoke(message: String) = pushSender.sendToAll(message)
}
