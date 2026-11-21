package br.com.mariofronza.sigabot.http.controller.user

import br.com.mariofronza.sigabot.http.controller.UserResponse
import br.com.mariofronza.sigabot.usecases.identity.RegisterStudent
import br.com.mariofronza.sigabot.usecases.identity.SearchUsers
import br.com.mariofronza.sigabot.usecases.shared.PageRequest
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
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
    private val searchUsers: SearchUsers,
) {

    @GetMapping
    fun search(
        @RequestParam query: String,
        @ParameterObject @PageableDefault(size = 20) pageable: Pageable,
    ): PagedModel<UserResponse> {
        val page = searchUsers(query, PageRequest(pageable.pageNumber, pageable.pageSize))
        return PagedModel(PageImpl(page.items.map(UserResponse::from), pageable, page.totalItems))
    }

    @PostMapping
    fun create(@Valid @RequestBody request: SignUpRequest): ResponseEntity<UserResponse> {
        val me = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/me").build().toUri()
        return ResponseEntity.created(me).body(UserResponse.from(registerStudent(request.toCommand())))
    }
}
