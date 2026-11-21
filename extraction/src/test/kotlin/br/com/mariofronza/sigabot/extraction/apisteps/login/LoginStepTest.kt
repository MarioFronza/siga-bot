package br.com.mariofronza.sigabot.extraction.apisteps.login

import br.com.mariofronza.sigabot.extraction.connector.Connector
import br.com.mariofronza.sigabot.extraction.dto.SemesterResultsExtractionInput
import br.com.mariofronza.sigabot.extraction.dto.SigaCredentialInput
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import br.com.mariofronza.sigabot.usecases.academic.port.SigaCredentialRejectedException
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class LoginStepTest : FunSpec({

    val server = MockWebServer()
    val input = SemesterResultsExtractionInput(SigaCredentialInput("00000000000", "wrong"), SyncScope.Latest)

    beforeSpec { server.start() }
    afterSpec { server.shutdown() }

    fun answer(body: String, vararg cookies: String) = server.enqueue(
        MockResponse().setHeader("Content-Type", "text/xml;charset=UTF-8").setBody(body)
            .apply { cookies.forEach { addHeader("Set-Cookie", it) } }
    )

    fun login(): ExtractorContext {
        val context = ExtractorContext(url = "sigaSecurityG5/login.jsf")
        LoginStep().execute(Connector(baseUrl = server.url("/").toString()).createConnection(), context, input)
        return context
    }

    test("reports a rejected credential when SIGA sends the login page back without a session") {
        answer(checkNotNull(javaClass.getResource("/siga/login-rejected.xml")).readText(), "JSESSIONID=abc; Path=/")

        shouldThrow<SigaCredentialRejectedException> { login() }
    }

    test("follows a login that opened a session") {
        answer(
            """<partial-response id="j_id1"><redirect url="https://siga.udesc.br/sigaSecurityG5/home.jsf"></redirect></partial-response>""",
            "JSESSIONID=abc", "etts=1", "mentor=2", "etp=3",
        )

        val context = login()

        context.url shouldBe "sigaSecurityG5/home.jsf"
        context.etts shouldBe "etts=1"
        context.etp shouldBe "etp=3"
    }
})
