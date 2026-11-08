package br.com.mariofronza.sigabot.http.controller.auth

import br.com.mariofronza.sigabot.usecases.auth.AuthService
import br.com.mariofronza.sigabot.usecases.misc.utils.Constants.Companion.AUTHORIZATION
import com.auth0.jwt.exceptions.JWTVerificationException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/refresh-token")
    fun refreshToken(@RequestHeader(AUTHORIZATION) authorization: String, request: HttpServletRequest) =
        authService.generateAccessToken(authorization, request.requestURL.toString())

    @ExceptionHandler(JWTVerificationException::class)
    @ResponseStatus(UNAUTHORIZED)
    fun rejectToken() = Unit
}
