package br.com.mariofronza.sigabot.extraction.extractor.dashboard

import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse
import br.com.mariofronza.sigabot.extraction.apisteps.dashboard.DashboardRedirectStep
import br.com.mariofronza.sigabot.extraction.apisteps.dashboard.DashboardStep
import br.com.mariofronza.sigabot.extraction.apisteps.login.LoginRedirectStep
import br.com.mariofronza.sigabot.extraction.apisteps.login.LoginStep
import br.com.mariofronza.sigabot.extraction.connector.RetrofitSigaConnector
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse.StepError
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SemesterResultsExtractionInput
import org.springframework.stereotype.Component

@Component
class DashboardExtractor(
    private val loginStep: LoginStep,
    private val loginRedirectStep: LoginRedirectStep,
    private val dashboardRedirectStep: DashboardRedirectStep,
    private val dashboardStep: DashboardStep
) {

    fun execute(
        connection: RetrofitSigaConnector,
        extractorContext: ExtractorContext,
        extractSemesterResultsInput: SemesterResultsExtractionInput
    ): StepResponse {
        loginStep.execute(connection, extractorContext, extractSemesterResultsInput).also {
            if (it is StepError) return it
        }

        loginRedirectStep.execute(connection, extractorContext).also {
            if (it is StepError) return it
        }

        dashboardRedirectStep.execute(connection, extractorContext).also {
            if (it is StepError) return it
        }

        return dashboardStep.execute(connection, extractorContext).also {
            if (it is StepError) return it
        }
    }
}
