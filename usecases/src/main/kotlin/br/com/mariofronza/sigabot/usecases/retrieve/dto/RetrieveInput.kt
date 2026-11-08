package br.com.mariofronza.sigabot.usecases.retrieve.dto

import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.PeriodInput
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SemesterResultsExtractionInput
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SigaCredentialInput

class RetrieveInput(
    val userId: Long = 0L,
    private val year: String = "",
    private val term: Int = 0
) {

    fun toSemesterResultsExtractionInput(sigaCredentialInput: SigaCredentialInput) = SemesterResultsExtractionInput(
        sigaCredentialInput = sigaCredentialInput,
        period = PeriodInput(
            year = year,
            term = term
        )
    )
}
