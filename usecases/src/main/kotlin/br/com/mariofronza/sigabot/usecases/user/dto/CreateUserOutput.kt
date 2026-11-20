package br.com.mariofronza.sigabot.usecases.user.dto

import java.util.UUID

data class CreateUserOutput(
    val id: UUID,
    val username: String,
    val email: String
)
