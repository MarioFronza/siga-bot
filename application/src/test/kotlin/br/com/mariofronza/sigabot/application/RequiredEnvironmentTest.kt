package br.com.mariofronza.sigabot.application

import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.FunSpec

class RequiredEnvironmentTest : FunSpec({

    val complete = mapOf(
        "DATASOURCE_URL" to "jdbc:postgresql://db:5432/sigabot",
        "DATASOURCE_USERNAME" to "sigabot",
        "DATASOURCE_PASSWORD" to "secret",
        "JWT_SECRET" to "a".repeat(32),
        "SECRETKEY" to "b".repeat(32),
        "ONESIGNAL_APIKEY" to "key",
        "SIGA_YEAR" to "2020",
        "SIGA_TERM" to "2"
    )

    test("names every missing or blank variable") {
        shouldThrowWithMessage<IllegalStateException>(
            "Missing required environment variables: DATASOURCE_URL, SIGA_TERM"
        ) {
            requireEnvironment(complete - "DATASOURCE_URL" + ("SIGA_TERM" to " "))
        }
    }
})
