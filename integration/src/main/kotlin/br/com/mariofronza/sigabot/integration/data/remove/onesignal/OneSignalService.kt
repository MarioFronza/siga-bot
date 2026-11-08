package br.com.mariofronza.sigabot.integration.data.remove.onesignal

import br.com.mariofronza.sigabot.integration.data.remove.onesignal.dto.*
import br.com.mariofronza.sigabot.integration.retrofit.connector.Connector
import br.com.mariofronza.sigabot.integration.retrofit.connector.OneSignalConnector
import br.com.mariofronza.sigabot.usecases.misc.config.ConfigService
import org.springframework.stereotype.Service

@Service
class OneSignalService(
    configService: ConfigService
) {

    private val oneSignalConnector: OneSignalConnector = Connector().createConnection()
    private val restApiKey = configService.getRequiredString("onesignal.apiKey")
    private val headers = mapOf(
        "Authorization" to "Basic $restApiKey",
    )

    fun sendMessageToAllUsers(message: String) {
        val oneSignalNotification = OneSignalNotification(
            app_id = APP_ID,
            included_segments = listOf("Subscribed Users"),
            data = OneSignalData(),
            contents = OneSignalContent(message)
        )
        oneSignalConnector.createNotification(headers, oneSignalNotification)
            .execute()
    }

    fun sendMessageToUser(userId: String, message: String) {
        val oneSignalNotification = OneSignalNotification(
            app_id = APP_ID,
            include_external_user_ids = listOf(userId),
            included_segments = emptyList(),
            data = OneSignalData(),
            contents = OneSignalContent(message)
        )
        oneSignalConnector.createNotification(headers, oneSignalNotification)
            .execute()
    }

    companion object {
        const val APP_ID = "98fe2c2e-e4e5-4953-aafb-46faac044c66"
    }
}
