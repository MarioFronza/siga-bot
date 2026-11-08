package br.com.mariofronza.sigabot.usecases.user.dto

data class CreateUserOutput(
    val id: Long,
    val username: String,
    val email: String
)
