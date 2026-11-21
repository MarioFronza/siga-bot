package br.com.mariofronza.sigabot.http.controller.notification

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class NotificationRequest(
    @field:NotBlank @field:Size(max = 500)
    val message: String,
)
