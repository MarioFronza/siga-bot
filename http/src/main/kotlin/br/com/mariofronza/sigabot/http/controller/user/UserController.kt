package br.com.mariofronza.sigabot.http.controller.user

import br.com.mariofronza.sigabot.usecases.user.UserService
import br.com.mariofronza.sigabot.usecases.user.dto.CreateUserInput
import br.com.mariofronza.sigabot.usecases.user.dto.CreateUserOutput
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping
    fun create(@RequestBody createUserRequest: CreateUserInput): ResponseEntity<CreateUserOutput> {
        val uri: URI = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/users").toUriString())
        return ResponseEntity.created(uri).body(userService.createUser(createUserRequest))
    }
}
