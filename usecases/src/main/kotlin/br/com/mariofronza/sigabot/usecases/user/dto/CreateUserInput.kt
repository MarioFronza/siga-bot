package br.com.mariofronza.sigabot.usecases.user.dto

data class CreateUserInput(
    val email: String,
    val password: String,
    val sigaCredential: SigaCredentialRequest
)

data class SigaCredentialRequest(
    val cpf: String,
    val password: String,
)
