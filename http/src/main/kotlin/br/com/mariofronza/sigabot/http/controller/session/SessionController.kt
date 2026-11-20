package br.com.mariofronza.sigabot.http.controller.session

import br.com.mariofronza.sigabot.usecases.identity.RefreshSession
import br.com.mariofronza.sigabot.usecases.identity.RevokeSession
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/{version}/sessions", version = "v1")
class SessionController(
    private val refreshSession: RefreshSession,
    private val revokeSession: RevokeSession,
) {

    @PostMapping("/refresh")
    fun refresh(@RequestBody input: RefreshSessionInput) = SessionResponse.from(refreshSession(input.refreshToken))

    @PostMapping("/revoke")
    @ResponseStatus(NO_CONTENT)
    fun revoke(@RequestBody input: RefreshSessionInput) {
        revokeSession(input.refreshToken)
    }
}
