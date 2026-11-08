package br.com.mariofronza.sigabot.usecases.misc.filter

import br.com.mariofronza.sigabot.usecases.misc.utils.Constants.Companion.AUTHORIZATION
import br.com.mariofronza.sigabot.usecases.misc.utils.Constants.Companion.BEARER
import br.com.mariofronza.sigabot.usecases.misc.utils.JWTUtil
import com.auth0.jwt.exceptions.JWTVerificationException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

class JWTAuthorizationFilter(
    authenticationManager: AuthenticationManager,
    private val jwtUtil: JWTUtil,
    private val userDetailService: UserDetailsService
) : BasicAuthenticationFilter(authenticationManager) {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val token = request.getHeader(AUTHORIZATION)
            ?.takeIf { it.startsWith("$BEARER ") }
            ?.removePrefix("$BEARER ")
            ?: return chain.doFilter(request, response)

        val user = loadUser(token) ?: return response.sendError(SC_UNAUTHORIZED)

        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(user, null, user.authorities)
        chain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest) = request.requestURI == "/auth/refresh-token"

    private fun loadUser(token: String): UserDetails? = try {
        userDetailService.loadUserByUsername(jwtUtil.decodeAccessToken(token).subject)
    } catch (_: JWTVerificationException) {
        null
    } catch (_: UsernameNotFoundException) {
        null
    }
}
