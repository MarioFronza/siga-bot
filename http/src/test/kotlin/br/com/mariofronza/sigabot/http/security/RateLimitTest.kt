package br.com.mariofronza.sigabot.http.security

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class RateLimitTest : FunSpec({

    class MovableClock(var now: Instant) : Clock() {
        override fun instant() = now
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId) = this
    }

    test("allows the limit per window and key, then starts over") {
        val clock = MovableClock(Instant.parse("2026-11-20T12:00:00Z"))
        val limiter = RateLimiter(limit = 2, window = Duration.ofMinutes(1), clock = clock)

        limiter.acquire("1.1.1.1").allowed shouldBe true
        limiter.acquire("1.1.1.1").allowed shouldBe true
        limiter.acquire("1.1.1.1").allowed shouldBe false
        limiter.acquire("2.2.2.2").allowed shouldBe true

        clock.now = clock.now.plusSeconds(60)
        limiter.acquire("1.1.1.1").allowed shouldBe true
    }

    test("tells a refused caller how long until the window starts over") {
        val clock = MovableClock(Instant.parse("2026-11-20T12:00:00Z"))
        val limiter = RateLimiter(limit = 1, window = Duration.ofMinutes(1), clock = clock)
        limiter.acquire("1.1.1.1")

        clock.now = clock.now.plusSeconds(20)

        limiter.acquire("1.1.1.1").retryAfter shouldBe Duration.ofSeconds(40)
    }

    test("answers 429 once an address goes over the limit on sign-up or login") {
        val filter = RateLimitFilter(RateLimiter(limit = 1, window = Duration.ofMinutes(1), clock = Clock.systemUTC()))
        fun login() = MockHttpServletResponse().also { response ->
            val chain = MockFilterChain()
            filter.doFilter(MockHttpServletRequest("POST", "/api/v1/sessions"), response, chain)
            if (response.status == 200) chain.request.shouldNotBeNull() else chain.request.shouldBeNull()
        }

        login().status shouldBe 200
        login().let {
            it.status shouldBe 429
            it.getHeader("Retry-After") shouldBe "60"
        }
    }

    test("leaves other routes alone") {
        val filter = RateLimitFilter(RateLimiter(limit = 0, window = Duration.ofMinutes(1), clock = Clock.systemUTC()))
        val chain = MockFilterChain()

        filter.doFilter(MockHttpServletRequest("GET", "/api/v1/me"), MockHttpServletResponse(), chain)

        chain.request.shouldNotBeNull()
    }
})
