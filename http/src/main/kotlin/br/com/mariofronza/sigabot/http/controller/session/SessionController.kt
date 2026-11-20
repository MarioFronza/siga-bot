package br.com.mariofronza.sigabot.http.controller.session

import br.com.mariofronza.sigabot.http.security.AuthService
import br.com.mariofronza.sigabot.http.controller.session.RefreshSessionInput
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/{version}/sessions", version = "v1")
class SessionController(
    private val authService: AuthService
) {

    @PostMapping("/refresh")
    fun refresh(@RequestBody input: RefreshSessionInput, request: HttpServletRequest) =
        authService.generateAccessToken(input.refreshToken, request.requestURL.toString())
}
