package br.com.mariofronza.sigabot.http.config

import jakarta.servlet.RequestDispatcher.ERROR_REQUEST_URI
import jakarta.servlet.RequestDispatcher.ERROR_STATUS_CODE
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.webmvc.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class ProblemErrorController : ErrorController {

    @RequestMapping("/error")
    fun error(request: HttpServletRequest): ResponseEntity<ProblemDetail> {
        val status = (request.getAttribute(ERROR_STATUS_CODE) as? Int)?.let(HttpStatus::resolve) ?: INTERNAL_SERVER_ERROR
        val problem = ProblemDetail.forStatus(status).apply {
            (request.getAttribute(ERROR_REQUEST_URI) as? String)?.let { instance = URI.create(it) }
        }
        return ResponseEntity.status(status).contentType(APPLICATION_PROBLEM_JSON).body(problem)
    }
}
