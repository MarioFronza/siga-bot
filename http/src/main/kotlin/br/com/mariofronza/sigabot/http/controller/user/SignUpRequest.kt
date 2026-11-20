package br.com.mariofronza.sigabot.http.controller.user

import br.com.mariofronza.sigabot.usecases.identity.RegisterStudent
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SignUpRequest(
    @field:Email @field:NotBlank
    val email: String,
    @field:NotBlank
    val password: String,
    @field:Valid
    val sigaCredential: SigaCredentialRequest,
) {
    data class SigaCredentialRequest(
        @field:NotBlank
        val cpf: String,
        @field:NotBlank
        val password: String,
    )

    fun toCommand() = RegisterStudent.Command(email, password, sigaCredential.cpf, sigaCredential.password)
}
