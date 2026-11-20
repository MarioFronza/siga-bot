package br.com.mariofronza.sigabot.http.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod.POST
import org.springframework.web.filter.OncePerRequestFilter

class RateLimitFilter(
    private val limiter: RateLimiter,
) : OncePerRequestFilter() {

    private val limited = Regex("^/api/[^/]+/(users|sessions|sessions/refresh)$")

    override fun shouldNotFilter(request: HttpServletRequest) =
        request.method != POST.name() || !limited.matches(request.requestURI)

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        if (limiter.tryAcquire("${request.remoteAddr} ${request.requestURI}")) chain.doFilter(request, response) else response.sendError(TOO_MANY_REQUESTS)
    }

    companion object {
        private const val TOO_MANY_REQUESTS = 429
    }
}
