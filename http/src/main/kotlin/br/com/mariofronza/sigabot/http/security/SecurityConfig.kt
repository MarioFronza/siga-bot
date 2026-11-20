package br.com.mariofronza.sigabot.http.security

import br.com.mariofronza.sigabot.usecases.identity.OpenSession
import org.springframework.beans.factory.annotation.Value
import java.time.Clock
import java.time.Duration
import jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.POST
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val userDetailsService: UserDetailsService,
    private val jwtUtil: JWTUtil,
    private val openSession: OpenSession,
    @param:Value("\${security.rate-limit.per-minute:10}")
    private val rateLimitPerMinute: Int,
) {

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(): AuthenticationManager = ProviderManager(
        DaoAuthenticationProvider(userDetailsService).apply { setPasswordEncoder(bCryptPasswordEncoder()) }
    )

    @Bean
    fun securityFilterChain(http: HttpSecurity, authenticationManager: AuthenticationManager): SecurityFilterChain {
        val authenticationFilter = JWTAuthenticationFilter(authenticationManager, openSession)
            .apply { setFilterProcessesUrl("/api/v1/sessions") }

        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(POST, "/api/*/users", "/api/*/sessions", "/api/*/sessions/refresh", "/api/*/sessions/revoke").permitAll()
                    .requestMatchers("/actuator/health/**", "/actuator/prometheus", "/error", "/api/*/openapi.json").permitAll()
                    .requestMatchers("/api/*/me/terms/**", "/api/*/me/syncs").hasAuthority("STUDENT")
                    .requestMatchers(POST, "/api/*/notifications", "/api/*/users/*/notifications").hasAuthority("ADMIN")
                    .anyRequest().authenticated()
            }
            .exceptionHandling { it.authenticationEntryPoint { _, response, _ -> response.sendError(SC_UNAUTHORIZED) } }
            .addFilterBefore(RateLimitFilter(RateLimiter(rateLimitPerMinute, Duration.ofMinutes(1), Clock.systemUTC())), UsernamePasswordAuthenticationFilter::class.java)
            .addFilter(authenticationFilter)
            .addFilterBefore(
                JWTAuthorizationFilter(authenticationManager, jwtUtil, userDetailsService),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .build()
    }
}
