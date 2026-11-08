package br.com.mariofronza.sigabot.extraction.extractor.prelogin

import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse
import br.com.mariofronza.sigabot.extraction.apisteps.prelogin.CookieStep
import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse.StepError
import br.com.mariofronza.sigabot.extraction.connector.RetrofitSigaConnector
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import org.springframework.stereotype.Component

@Component
class CookieExtractor(
    private val cookieStep: CookieStep
) {

    fun execute(connection: RetrofitSigaConnector, extractorContext: ExtractorContext): StepResponse {
        return cookieStep.execute(connection, extractorContext).also {
            if (it is StepError) return it
        }
    }
}
