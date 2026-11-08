package br.com.mariofronza.sigabot.usecases.extraction.semester

import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SemesterResultsExtractionInput
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SemesterResultsExtractionOutput

interface SemesterResultsExtraction {

    fun extract(
        extractSemesterResultsInput: SemesterResultsExtractionInput,
        completeExtraction: Boolean
    ): SemesterResultsExtractionOutput
}
