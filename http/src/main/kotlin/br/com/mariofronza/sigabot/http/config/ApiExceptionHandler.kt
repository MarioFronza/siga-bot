package br.com.mariofronza.sigabot.http.config

import org.springframework.http.HttpStatus.BAD_REQUEST
import br.com.mariofronza.sigabot.entity.user.AcademicAccountAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.EmailAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.UserNotFoundException
import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(EmailAlreadyRegisteredException::class, AcademicAccountAlreadyRegisteredException::class)
    fun conflict(e: RuntimeException): ProblemDetail = ProblemDetail.forStatusAndDetail(CONFLICT, e.message)

    @ExceptionHandler(UserNotFoundException::class)
    fun notFound(e: UserNotFoundException): ProblemDetail = ProblemDetail.forStatusAndDetail(NOT_FOUND, e.message)

    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(e: IllegalArgumentException): ProblemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, e.message)

    @ExceptionHandler(JWTVerificationException::class)
    fun unauthorized(): ProblemDetail = ProblemDetail.forStatusAndDetail(UNAUTHORIZED, "Invalid refresh token")
}
