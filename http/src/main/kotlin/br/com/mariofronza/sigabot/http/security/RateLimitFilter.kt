package br.com.mariofronza.sigabot.http.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders.RETRY_AFTER
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.web.filter.OncePerRequestFilter

class RateLimitFilter(
    private val limiter: RateLimiter,
) : OncePerRequestFilter() {

    private val limited = mapOf(
        POST.name() to Regex("^/api/[^/]+/(users|sessions|sessions/refresh|me/syncs)$"),
        PUT.name() to Regex("^/api/[^/]+/me/siga-credential$"),
    )

    override fun shouldNotFilter(request: HttpServletRequest) =
        limited[request.method]?.matches(request.requestURI) != true

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val admission = limiter.acquire("${request.remoteAddr} ${request.requestURI}")
        if (admission.allowed) return chain.doFilter(request, response)
        response.setHeader(RETRY_AFTER, admission.retryAfter.plusMillis(999).toSeconds().coerceAtLeast(1).toString())
        response.sendError(TOO_MANY_REQUESTS)
    }

    companion object {
        private const val TOO_MANY_REQUESTS = 429
    }
}
