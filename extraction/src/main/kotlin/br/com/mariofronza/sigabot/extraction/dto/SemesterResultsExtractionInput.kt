package br.com.mariofronza.sigabot.extraction.dto

import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope

data class SemesterResultsExtractionInput(
    val sigaCredentialInput: SigaCredentialInput,
    val scope: SyncScope,
)

data class SigaCredentialInput(
    val cpf: String,
    val password: String,
)
