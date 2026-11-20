package br.com.mariofronza.sigabot.http.controller.session

import br.com.mariofronza.sigabot.usecases.identity.Session

data class SessionResponse(val accessToken: String, val refreshToken: String, val userId: String) {
    companion object {
        fun from(session: Session) = SessionResponse(session.accessToken, session.refreshToken, session.userId.toString())
    }
}
