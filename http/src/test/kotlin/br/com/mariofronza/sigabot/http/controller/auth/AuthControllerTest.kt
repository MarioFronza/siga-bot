package br.com.mariofronza.sigabot.http.controller.auth

import br.com.mariofronza.sigabot.usecases.auth.AuthService
import com.auth0.jwt.exceptions.JWTVerificationException
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class AuthControllerTest : FunSpec({

    test("answers 401 when the refresh token is rejected") {
        val authService: AuthService = mockk()
        every { authService.generateAccessToken(any(), any()) } throws JWTVerificationException("rejected")
        val mockMvc = MockMvcBuilders.standaloneSetup(AuthController(authService)).build()

        mockMvc.perform(post("/auth/refresh-token").header("Authorization", "Bearer token"))
            .andExpect(status().isUnauthorized)
    }
})
