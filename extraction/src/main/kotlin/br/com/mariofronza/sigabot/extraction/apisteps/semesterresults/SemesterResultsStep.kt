package br.com.mariofronza.sigabot.extraction.apisteps.semesterresults

import br.com.mariofronza.sigabot.extraction.apisteps.Step
import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse
import br.com.mariofronza.sigabot.extraction.connector.RetrofitSigaConnector
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import org.springframework.stereotype.Component

@Component
class SemesterResultsStep : Step() {

    fun execute(connection: RetrofitSigaConnector, extractorContext: ExtractorContext): StepResponse {
        val headers = mapOf(
            "Cookie" to "${extractorContext.headerSessionId}; ECS=S; ${extractorContext.etts}"
        )

        return connection.semesterResults(extractorContext.url, headers)
            .execute()
            .updateSessionId(extractorContext, position = 0)
            .getBody()
    }
}
