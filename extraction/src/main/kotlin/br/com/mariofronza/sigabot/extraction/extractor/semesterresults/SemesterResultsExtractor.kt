package br.com.mariofronza.sigabot.extraction.extractor.semesterresults

import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse
import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse.StepError
import br.com.mariofronza.sigabot.extraction.apisteps.semesterresults.SemesterResultsWithQueryStep
import br.com.mariofronza.sigabot.extraction.connector.RetrofitSigaConnector
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import org.springframework.stereotype.Component

@Component
class SemesterResultsExtractor(
    private val semesterResultsWithQueryStep: SemesterResultsWithQueryStep
) {

    fun execute(
        connection: RetrofitSigaConnector,
        extractorContext: ExtractorContext,
        periodIdentified: String
    ): StepResponse {
        return semesterResultsWithQueryStep.execute(connection, extractorContext, periodIdentified).also {
            if (it is StepError) return it
        }
    }
}
