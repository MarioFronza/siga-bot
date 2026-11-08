package br.com.mariofronza.sigabot.integration.notification

import br.com.mariofronza.sigabot.integration.data.remove.onesignal.OneSignalService
import br.com.mariofronza.sigabot.usecases.notification.PushNotification
import org.springframework.stereotype.Service

@Service
class OnePushNotificationService(
    private val oneSignalService: OneSignalService
) : PushNotification {

    override fun notifyAllUsers(message: String) {
        oneSignalService.sendMessageToAllUsers(message)
    }

    override fun notifyUser(userId: Long, message: String) {
        oneSignalService.sendMessageToUser(userId.toString(), message)
    }
}
