package br.com.mariofronza.sigabot.http.controller.notification

import jakarta.validation.constraints.NotBlank

data class NotificationRequest(
    @field:NotBlank
    val message: String,
)
