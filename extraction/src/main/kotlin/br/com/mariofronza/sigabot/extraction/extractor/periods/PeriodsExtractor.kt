package br.com.mariofronza.sigabot.extraction.extractor.periods

import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse
import br.com.mariofronza.sigabot.extraction.apisteps.semesterresults.SemesterResultsStep
import br.com.mariofronza.sigabot.extraction.connector.RetrofitSigaConnector
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import org.springframework.stereotype.Component

@Component
class PeriodsExtractor(
    private val semesterResultsStep: SemesterResultsStep,
) {

    fun execute(
        connection: RetrofitSigaConnector,
        extractorContext: ExtractorContext,
    ): StepResponse {
        return semesterResultsStep.execute(connection, extractorContext).also {
            if (it is StepResponse.StepError) return it
        }
    }
}
