package br.com.mariofronza.sigabot.extraction.apisteps.login

import br.com.mariofronza.sigabot.extraction.apisteps.Step
import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse
import br.com.mariofronza.sigabot.extraction.connector.RetrofitSigaConnector
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import org.springframework.stereotype.Component

@Component
class LoginRedirectStep : Step() {

    fun execute(connection: RetrofitSigaConnector, extractorContext: ExtractorContext): StepResponse {
        val headers = mapOf(
            "Cookie" to extractorContext.headerSessionId
        )

        return connection.signInRedirect(extractorContext.url, headers)
            .execute()
            .updateViewState(extractorContext)
            .getBody()
    }
}
