package br.com.mariofronza.sigabot.usecases.misc.filter

import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.misc.utils.Constants.Companion.AUTHORIZATION
import br.com.mariofronza.sigabot.usecases.misc.utils.Constants.Companion.BEARER
import br.com.mariofronza.sigabot.usecases.misc.utils.JWTUtil
import tools.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class JWTAuthenticationFilter(
    authenticationManager: AuthenticationManager,
    private val jwtUtil: JWTUtil
) : UsernamePasswordAuthenticationFilter(authenticationManager) {

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        try {
            val (email, password) = jacksonObjectMapper().readValue(request.inputStream, Credentials::class.java)

            val token = UsernamePasswordAuthenticationToken(email, password)

            return authenticationManager.authenticate(token)
        } catch (e: Exception) {
            throw UsernameNotFoundException("User not found!")
        }
    }

    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        authResult: Authentication
    ) {
        val user = (authResult.principal as UserDetailsImpl)
        val accessToken = jwtUtil.generateAccessToken(user, request.requestURI)
        val refreshToken = jwtUtil.generateRefreshToken(user, request.requestURI)

        response.addHeader(AUTHORIZATION, "$BEARER $accessToken")
        val tokenMap = mapOf(
            "accessToken" to accessToken,
            "refreshToken" to refreshToken,
            "userId" to user.toUser().id.toString()
        )

        jacksonObjectMapper().writeValue(response.outputStream, tokenMap)
    }
}

data class Credentials(
    val email: String = "",
    val password: String = ""
)

class UserDetailsImpl(private val user: User) : UserDetails {

    override fun getAuthorities() = user.roles.map { SimpleGrantedAuthority(it.name) }

    override fun isEnabled() = true

    override fun getUsername() = user.email

    override fun isCredentialsNonExpired() = true

    override fun getPassword() = user.passwordHash

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    fun toUser() = User(
        id = user.id,
        fullName = user.fullName,
        isActive = user.isActive,
        email = user.email,
        passwordHash = password,
        sigaCredential = user.sigaCredential,
        roles = user.roles,
        groups = user.groups
    )
}
