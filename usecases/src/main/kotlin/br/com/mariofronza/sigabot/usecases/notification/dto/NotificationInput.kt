package br.com.mariofronza.sigabot.usecases.notification.dto

import jakarta.validation.constraints.NotBlank

data class NotificationInput(
    @field:NotBlank
    val message: String,
)
