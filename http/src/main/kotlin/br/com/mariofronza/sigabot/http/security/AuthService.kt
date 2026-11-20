package br.com.mariofronza.sigabot.http.security

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
            "userId" to user.toUser().id.toString()
        )
    }
}
