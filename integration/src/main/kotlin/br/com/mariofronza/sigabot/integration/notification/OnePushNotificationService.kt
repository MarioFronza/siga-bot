package br.com.mariofronza.sigabot.integration.notification

import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.integration.data.remove.onesignal.OneSignalService
import br.com.mariofronza.sigabot.usecases.notification.port.PushSender
import org.springframework.stereotype.Service

@Service
class OnePushNotificationService(
    private val oneSignalService: OneSignalService
) : PushSender {

    override fun sendToAll(message: String) {
        oneSignalService.sendMessageToAllUsers(message)
    }

    override fun sendTo(recipient: UserId, message: String) {
        oneSignalService.sendMessageToUser(recipient.toString(), message)
    }
}
