package br.com.mariofronza.sigabot.usecases.notification.port

import br.com.mariofronza.sigabot.entity.user.UserId

interface PushSender {

    fun sendToAll(message: String)

    fun sendTo(recipient: UserId, message: String)
}
