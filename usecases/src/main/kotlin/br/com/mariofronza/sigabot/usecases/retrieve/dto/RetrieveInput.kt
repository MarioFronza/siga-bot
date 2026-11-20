package br.com.mariofronza.sigabot.usecases.retrieve.dto

import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.PeriodInput
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SemesterResultsExtractionInput
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SigaCredentialInput

class RetrieveInput(
    private val year: Int = 0,
    private val term: Int = 0
) {

    fun toSemesterResultsExtractionInput(sigaCredentialInput: SigaCredentialInput) = SemesterResultsExtractionInput(
        sigaCredentialInput = sigaCredentialInput,
        period = PeriodInput(
            year = year.toString(),
            term = term
        )
    )
}
