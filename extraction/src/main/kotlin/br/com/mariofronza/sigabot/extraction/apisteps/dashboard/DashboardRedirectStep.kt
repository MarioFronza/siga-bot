package br.com.mariofronza.sigabot.extraction.apisteps.dashboard

import br.com.mariofronza.sigabot.extraction.apisteps.Step
import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse
import br.com.mariofronza.sigabot.extraction.connector.RetrofitSigaConnector
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import org.springframework.stereotype.Component

@Component
class DashboardRedirectStep : Step() {

    fun execute(connection: RetrofitSigaConnector, extractorContext: ExtractorContext): StepResponse {
        val headers = mapOf(
            "Cookie" to extractorContext.headerSessionId,
        )

        val body = mapOf(
            "javax.faces.partial.ajax" to "true",
            "javax.faces.source" to "formRedirect:j_idt34",
            "javax.faces.partial.execute" to "formRedirect:j_idt34",
            "javax.faces.partial.render" to "@none",
            "formRedirect:j_idt34" to "formRedirect:j_idt34",
            "formRedirect" to "formRedirect",
            "javax.faces.ViewState" to extractorContext.viewState,
            "csrfToken" to extractorContext.csrfToken,
        )

        return connection.dashboardRedirect(headers, body)
            .execute()
            .updateEndpoint(extractorContext)
            .getBody()
    }
}
