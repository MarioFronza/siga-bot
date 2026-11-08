package br.com.mariofronza.sigabot.extraction.apisteps.dashboard

import br.com.mariofronza.sigabot.extraction.apisteps.Step
import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse
import br.com.mariofronza.sigabot.extraction.connector.RetrofitSigaConnector
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import org.springframework.stereotype.Component

@Component
class DashboardStep : Step() {

    fun execute(connection: RetrofitSigaConnector, extractorContext: ExtractorContext): StepResponse {
        val headers = mapOf(
            "Cookie" to "${extractorContext.headerSessionId}; ECS=S; ${extractorContext.etts}"
        )

        return connection.dashboard(extractorContext.url, headers)
            .execute()
            .getBody()
    }
}
