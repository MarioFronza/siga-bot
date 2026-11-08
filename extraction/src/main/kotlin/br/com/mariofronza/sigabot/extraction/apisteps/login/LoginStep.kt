package br.com.mariofronza.sigabot.extraction.apisteps.login

import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse
import br.com.mariofronza.sigabot.extraction.connector.RetrofitSigaConnector
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SemesterResultsExtractionInput
import org.springframework.stereotype.Component
import br.com.mariofronza.sigabot.extraction.apisteps.Step as Step1

@Component
class LoginStep : Step1() {

    fun execute(
        connection: RetrofitSigaConnector,
        extractorContext: ExtractorContext,
        extractSemesterResultsInput: SemesterResultsExtractionInput
    ): StepResponse {
        val headers = mapOf(
            "Cookie" to extractorContext.headerSessionId,
        )

        val sigaCredential = extractSemesterResultsInput.sigaCredentialInput

        val body = mapOf(
            "javax.faces.partial.ajax" to "true",
            "javax.faces.source" to "loginForm:realizaLogin",
            "javax.faces.partial.execute" to "loginForm:realizaLogin",
            "javax.faces.partial.render" to "loginForm:realizaLogin",
            "loginForm:realizaLogin" to "loginForm:realizaLogin",
            "tipoLogin" to "PADRAO",
            "login" to sigaCredential.cpf,
            "senha" to sigaCredential.password,
            "mantemConectado" to "S",
            "loginForm" to "loginForm",
            "javax.faces.ViewState" to extractorContext.viewState,
            "csrfToken" to extractorContext.csrfToken,
        )

        return connection.signIn(extractorContext.url, headers, body)
            .execute()
            .updateEndpoint(extractorContext)
            .updateCookies(extractorContext, ettsPosition = 1, etpPosition = 3)
            .getBody()
    }
}
