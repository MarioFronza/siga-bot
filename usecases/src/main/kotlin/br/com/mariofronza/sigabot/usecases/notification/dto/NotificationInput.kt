package br.com.mariofronza.sigabot.usecases.notification.dto

data class NotificationInput(
    val description: String,
    val entityId: Long,
)
