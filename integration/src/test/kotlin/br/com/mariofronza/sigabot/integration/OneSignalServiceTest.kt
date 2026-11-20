package br.com.mariofronza.sigabot.integration

import br.com.mariofronza.sigabot.integration.data.remove.onesignal.OneSignalRejectedException
import br.com.mariofronza.sigabot.integration.data.remove.onesignal.OneSignalService
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class OneSignalServiceTest : FunSpec({

    val server = MockWebServer()
    lateinit var oneSignal: OneSignalService

    beforeSpec { server.start() }
    afterSpec { server.shutdown() }
    beforeTest { oneSignal = OneSignalService("key", server.url("/api/v1/notifications/").toString()) }

    test("sends a push to one user with the API key") {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"id":"n1"}"""))

        shouldNotThrowAny { oneSignal.sendMessageToUser("user-1", "oi") }

        val request = server.takeRequest()
        request.getHeader("Authorization") shouldBe "Basic key"
        request.body.readUtf8() shouldContain "user-1"
    }

    test("fails when OneSignal rejects the push, so the outbox retries it") {
        server.enqueue(MockResponse().setResponseCode(429).setBody("""{"errors":["rate limited"]}"""))

        val error = shouldThrow<OneSignalRejectedException> { oneSignal.sendMessageToUser("user-1", "oi") }

        error.message shouldContain "429"
        server.takeRequest()
    }

    test("fails when a broadcast is rejected") {
        server.enqueue(MockResponse().setResponseCode(500))

        shouldThrow<OneSignalRejectedException> { oneSignal.sendMessageToAllUsers("oi") }
        server.takeRequest()
    }
})

class OneSignalTimeoutTest : FunSpec({

    test("gives up on a OneSignal that does not answer in time, so the outbox retries") {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("{}").setHeadersDelay(2, java.util.concurrent.TimeUnit.SECONDS))
            val oneSignal = OneSignalService("key", server.url("/api/v1/notifications/").toString(), callTimeout = java.time.Duration.ofMillis(300))

            shouldThrow<java.io.InterruptedIOException> { oneSignal.sendMessageToUser("user-1", "oi") }
        }
    }
})
