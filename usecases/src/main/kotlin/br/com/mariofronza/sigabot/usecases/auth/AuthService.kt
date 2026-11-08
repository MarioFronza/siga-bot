package br.com.mariofronza.sigabot.usecases.auth

import br.com.mariofronza.sigabot.usecases.misc.filter.UserDetailsImpl
import br.com.mariofronza.sigabot.usecases.misc.utils.Constants
import br.com.mariofronza.sigabot.usecases.misc.utils.JWTUtil
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class AuthService(
    private var jwtUtil: JWTUtil,
    private var userDetailService: UserDetailsService
) {

    fun generateAccessToken(authorizationHeader: String, requestUrl: String): Map<String, String> {
        if (authorizationHeader.startsWith(Constants.BEARER)) {
            val token = authorizationHeader.substring("Bearer ".length)
            val decodedJWT = jwtUtil.decodeRefreshToken(token)
            val user = userDetailService.loadUserByUsername(decodedJWT.subject) as UserDetailsImpl

            return mapOf(
                "accessToken" to jwtUtil.generateAccessToken(user, requestUrl),
                "refreshToken" to jwtUtil.generateRefreshToken(user, requestUrl),
                "userId" to user.toUser().id.toString()
            )
        } else {
            throw RuntimeException("Refresh token is missing")
        }
    }
}
