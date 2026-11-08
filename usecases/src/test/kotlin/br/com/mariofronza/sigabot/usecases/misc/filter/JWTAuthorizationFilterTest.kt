package br.com.mariofronza.sigabot.usecases.misc.filter

import br.com.mariofronza.sigabot.usecases.misc.utils.JWTUtil
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.AuthenticationManager

class JWTAuthorizationFilterTest : FunSpec({

    val filter = JWTAuthorizationFilter(
        mockk<AuthenticationManager>(),
        JWTUtil("test-secret-test-secret-test-secret-0123"),
        mockk()
    )

    test("lets requests without authorization header through") {
        val chain = MockFilterChain()

        filter.doFilter(MockHttpServletRequest("GET", "/notifications/user/1"), MockHttpServletResponse(), chain)

        chain.request.shouldNotBeNull()
    }

    test("stops the chain with 401 on an invalid token") {
        val request = MockHttpServletRequest("GET", "/notifications").apply { addHeader("Authorization", "Bearer invalid") }
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        response.status shouldBe 401
        chain.request.shouldBeNull()
    }
})
