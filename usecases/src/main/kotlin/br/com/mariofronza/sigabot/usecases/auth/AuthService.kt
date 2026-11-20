package br.com.mariofronza.sigabot.usecases.auth

import br.com.mariofronza.sigabot.usecases.misc.filter.UserDetailsImpl
import br.com.mariofronza.sigabot.usecases.misc.utils.JWTUtil
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class AuthService(
    private var jwtUtil: JWTUtil,
    private var userDetailService: UserDetailsService
) {

    fun generateAccessToken(refreshToken: String, requestUrl: String): Map<String, String> {
        val decodedJWT = jwtUtil.decodeRefreshToken(refreshToken)
        val user = userDetailService.loadUserByUsername(decodedJWT.subject) as UserDetailsImpl

        return mapOf(
            "accessToken" to jwtUtil.generateAccessToken(user, requestUrl),
            "refreshToken" to jwtUtil.generateRefreshToken(user, requestUrl),
            "userId" to user.toUser().requireExternalId().toString()
        )
    }
}
