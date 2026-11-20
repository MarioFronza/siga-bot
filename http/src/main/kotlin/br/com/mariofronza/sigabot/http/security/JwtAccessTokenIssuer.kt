package br.com.mariofronza.sigabot.http.security

import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.identity.port.AccessTokenIssuer
import org.springframework.stereotype.Component

@Component
class JwtAccessTokenIssuer(
    private val jwtUtil: JWTUtil,
) : AccessTokenIssuer {

    override fun issue(user: User): String = jwtUtil.generateAccessToken(UserDetailsImpl(user))
}
