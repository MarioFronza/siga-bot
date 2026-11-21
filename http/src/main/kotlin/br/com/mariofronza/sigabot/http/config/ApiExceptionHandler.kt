package br.com.mariofronza.sigabot.http.config

import br.com.mariofronza.sigabot.usecases.academic.port.SigaLayoutChangedException
import br.com.mariofronza.sigabot.usecases.academic.port.SigaPeriodNotFoundException
import br.com.mariofronza.sigabot.usecases.academic.port.SigaUnavailableException
import org.springframework.http.HttpStatus.BAD_GATEWAY
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.HttpStatus.BAD_REQUEST
import br.com.mariofronza.sigabot.entity.user.AcademicAccountAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.EmailAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.UserNotFoundException
import br.com.mariofronza.sigabot.entity.session.InvalidRefreshTokenException
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.context.request.WebRequest
import java.net.URI

@RestControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {

    data class InvalidField(val field: String, val message: String?)

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        ex.body.setProperty("errors", ex.bindingResult.fieldErrors.map { InvalidField(it.field, it.defaultMessage) })
        return super.handleMethodArgumentNotValid(ex, headers, status, request)
    }

    @ExceptionHandler(EmailAlreadyRegisteredException::class)
    fun emailTaken(e: EmailAlreadyRegisteredException) = conflict(e, "email-already-registered", field = "email")

    @ExceptionHandler(AcademicAccountAlreadyRegisteredException::class)
    fun credentialTaken(e: AcademicAccountAlreadyRegisteredException) = conflict(e, "siga-credential-already-registered", field = "cpf")

    private fun conflict(e: RuntimeException, problem: String, field: String) =
        ProblemDetail.forStatusAndDetail(CONFLICT, e.message).apply {
            type = URI.create("urn:siga-bot:problem:$problem")
            setProperty("field", field)
        }

    @ExceptionHandler(UserNotFoundException::class)
    fun notFound(e: UserNotFoundException): ProblemDetail = ProblemDetail.forStatusAndDetail(NOT_FOUND, e.message)

    @ExceptionHandler(SigaPeriodNotFoundException::class)
    fun periodNotFound(e: SigaPeriodNotFoundException): ProblemDetail = ProblemDetail.forStatusAndDetail(NOT_FOUND, e.message)

    @ExceptionHandler(SigaUnavailableException::class)
    fun sigaUnavailable(e: SigaUnavailableException): ProblemDetail = ProblemDetail.forStatusAndDetail(SERVICE_UNAVAILABLE, e.message)

    @ExceptionHandler(SigaLayoutChangedException::class)
    fun sigaChanged(e: SigaLayoutChangedException): ProblemDetail = ProblemDetail.forStatusAndDetail(BAD_GATEWAY, e.message)

    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(e: IllegalArgumentException): ProblemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, e.message)

    @ExceptionHandler(InvalidRefreshTokenException::class)
    fun unauthorized(e: InvalidRefreshTokenException): ProblemDetail = ProblemDetail.forStatusAndDetail(UNAUTHORIZED, e.message)
}
