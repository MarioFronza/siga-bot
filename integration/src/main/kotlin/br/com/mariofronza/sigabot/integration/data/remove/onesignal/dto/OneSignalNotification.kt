package br.com.mariofronza.sigabot.integration.data.remove.onesignal.dto

data class OneSignalNotification(
    val app_id: String,
    val included_segments: List<String> = listOf("Subscribed Users"),
    val include_external_user_ids: List<String> = emptyList(),
    val data: OneSignalData,
    val contents: OneSignalContent
)

data class OneSignalData(
    val foo: String = "bar"
)

data class OneSignalContent(
    val en: String
)
