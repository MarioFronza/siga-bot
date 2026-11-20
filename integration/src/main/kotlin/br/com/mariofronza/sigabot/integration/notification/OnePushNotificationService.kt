package br.com.mariofronza.sigabot.integration.notification

import br.com.mariofronza.sigabot.integration.data.remove.onesignal.OneSignalService
import br.com.mariofronza.sigabot.usecases.notification.PushNotification
import org.springframework.stereotype.Service
import br.com.mariofronza.sigabot.entity.user.UserId

@Service
class OnePushNotificationService(
    private val oneSignalService: OneSignalService
) : PushNotification {

    override fun notifyAllUsers(message: String) {
        oneSignalService.sendMessageToAllUsers(message)
    }

    override fun notifyUser(userId: UserId, message: String) {
        oneSignalService.sendMessageToUser(userId.toString(), message)
    }
}
