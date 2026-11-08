package br.com.mariofronza.sigabot.extraction.extractor.semesterresults

import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse
import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse.StepError
import br.com.mariofronza.sigabot.extraction.apisteps.semesterresults.DashboardSemesterResultsStep
import br.com.mariofronza.sigabot.extraction.connector.RetrofitSigaConnector
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import org.springframework.stereotype.Component

@Component
class DashboardSemesterResultsExtractor(
    private val dashboardSemesterResultsStep: DashboardSemesterResultsStep
) {

    fun execute(connection: RetrofitSigaConnector, extractorContext: ExtractorContext): StepResponse {
        return dashboardSemesterResultsStep.execute(connection, extractorContext).also {
            if (it is StepError) return it
        }
    }
}
