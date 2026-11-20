package br.com.mariofronza.sigabot.extraction.connector

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.InterruptedIOException
import java.time.Duration
import java.util.concurrent.TimeUnit

class ConnectorTest : FunSpec({

    test("gives up on a SIGA that keeps trickling a response") {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("<html>".repeat(100)).throttleBody(10, 1, TimeUnit.SECONDS))
            val siga = Connector(server.url("/").toString(), callTimeout = Duration.ofMillis(500)).createConnection()

            shouldThrow<InterruptedIOException> { siga.loginPage().execute() }
        }
    }
})
