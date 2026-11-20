package br.com.mariofronza.sigabot.usecases.user.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateUserInput(
    @field:Email @field:NotBlank
    val email: String,
    @field:NotBlank
    val password: String,
    @field:Valid
    val sigaCredential: SigaCredentialRequest
)

data class SigaCredentialRequest(
    @field:NotBlank
    val cpf: String,
    @field:NotBlank
    val password: String,
)
