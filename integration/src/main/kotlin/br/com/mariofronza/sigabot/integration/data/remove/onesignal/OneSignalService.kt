package br.com.mariofronza.sigabot.integration.data.remove.onesignal

import br.com.mariofronza.sigabot.integration.data.remove.onesignal.dto.*
import br.com.mariofronza.sigabot.integration.retrofit.connector.Connector
import br.com.mariofronza.sigabot.integration.retrofit.connector.OneSignalConnector
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class OneSignalService(
    @param:Value("\${onesignal.apiKey}")
    private val restApiKey: String,
    @param:Value("\${onesignal.base-url:https://onesignal.com/api/v1/notifications/}")
    baseUrl: String,
) {

    private val oneSignalConnector: OneSignalConnector = Connector().createConnection(baseUrl)
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
        send(oneSignalNotification)
    }

    fun sendMessageToUser(userId: String, message: String) {
        val oneSignalNotification = OneSignalNotification(
            app_id = APP_ID,
            include_external_user_ids = listOf(userId),
            included_segments = emptyList(),
            data = OneSignalData(),
            contents = OneSignalContent(message)
        )
        send(oneSignalNotification)
    }

    private fun send(notification: OneSignalNotification) {
        val response = oneSignalConnector.createNotification(headers, notification).execute()
        if (!response.isSuccessful) {
            throw OneSignalRejectedException(response.code(), response.errorBody()?.string().orEmpty())
        }
    }

    companion object {
        const val APP_ID = "98fe2c2e-e4e5-4953-aafb-46faac044c66"
    }
}
