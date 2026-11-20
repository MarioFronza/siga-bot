package br.com.mariofronza.sigabot.extraction.dto

data class SemesterResultsExtractionInput(
    val sigaCredentialInput: SigaCredentialInput,
    val period: PeriodInput
)

data class SigaCredentialInput(
    val cpf: String,
    val password: String,
)

data class PeriodInput(
    val year: String,
    val term: Int,
)
