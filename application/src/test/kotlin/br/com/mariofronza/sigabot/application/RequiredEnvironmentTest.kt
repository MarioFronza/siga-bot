package br.com.mariofronza.sigabot.application

import io.kotest.assertions.throwables.shouldNotThrowAny
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
    )

    test("names every missing or blank variable") {
        shouldThrowWithMessage<IllegalStateException>(
            "Missing required environment variables: DATASOURCE_URL, ONESIGNAL_APIKEY"
        ) {
            requireEnvironment(complete - "DATASOURCE_URL" + ("ONESIGNAL_APIKEY" to " "))
        }
    }

    test("starts without a SIGA period, since the latest one SIGA lists is the default") {
        shouldNotThrowAny { requireEnvironment(complete) }
    }
})
