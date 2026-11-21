package br.com.mariofronza.sigabot.http.controller.me

import br.com.mariofronza.sigabot.usecases.identity.UpdateSigaCredential
import jakarta.validation.constraints.NotBlank

data class SigaCredentialRequest(
    @field:NotBlank
    val cpf: String,
    @field:NotBlank
    val password: String,
) {
    fun toCommand() = UpdateSigaCredential.Command(cpf, password)
}
