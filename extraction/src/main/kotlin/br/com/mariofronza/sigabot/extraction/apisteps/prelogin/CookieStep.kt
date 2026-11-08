package br.com.mariofronza.sigabot.extraction.apisteps.prelogin

import br.com.mariofronza.sigabot.extraction.apisteps.Step
import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse
import br.com.mariofronza.sigabot.extraction.connector.RetrofitSigaConnector
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import org.springframework.stereotype.Component

@Component
class CookieStep : Step() {

    fun execute(connection: RetrofitSigaConnector, extractorContext: ExtractorContext): StepResponse {
        return connection.loginPage()
            .execute()
            .updateViewState(extractorContext)
            .updateSessionId(extractorContext)
            .getBody()
    }
}
