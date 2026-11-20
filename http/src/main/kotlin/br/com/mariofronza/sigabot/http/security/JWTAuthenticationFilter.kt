package br.com.mariofronza.sigabot.http.security

import br.com.mariofronza.sigabot.http.controller.session.SessionResponse
import br.com.mariofronza.sigabot.usecases.identity.OpenSession
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import br.com.mariofronza.sigabot.entity.user.User
import org.springframework.http.HttpHeaders.AUTHORIZATION
import br.com.mariofronza.sigabot.http.security.JWTUtil.Companion.BEARER
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
    private val openSession: OpenSession,
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
        val session = openSession((authResult.principal as UserDetailsImpl).toUser())
        response.addHeader(AUTHORIZATION, "$BEARER ${session.accessToken}")
        response.contentType = APPLICATION_JSON_VALUE
        jacksonObjectMapper().writeValue(response.outputStream, SessionResponse.from(session))
    }
}

data class Credentials(
    val email: String = "",
    val password: String = ""
)

class UserDetailsImpl(private val user: User) : UserDetails {

    override fun getAuthorities() = user.roles.map { SimpleGrantedAuthority(it.name) }

    override fun isEnabled() = true

    override fun getUsername() = user.email.value

    override fun isCredentialsNonExpired() = true

    override fun getPassword() = user.passwordHash

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    fun toUser() = user
}
