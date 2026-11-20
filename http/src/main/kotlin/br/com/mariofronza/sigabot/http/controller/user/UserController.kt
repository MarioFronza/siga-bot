package br.com.mariofronza.sigabot.http.controller.user

import br.com.mariofronza.sigabot.http.controller.UserResponse
import br.com.mariofronza.sigabot.usecases.identity.RegisterStudent
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/{version}/users", version = "v1")
class UserController(
    private val registerStudent: RegisterStudent,
) {

    @PostMapping
    fun create(@Valid @RequestBody request: SignUpRequest): ResponseEntity<UserResponse> {
        val me = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/me").build().toUri()
        return ResponseEntity.created(me).body(UserResponse.from(registerStudent(request.toCommand())))
    }
}
