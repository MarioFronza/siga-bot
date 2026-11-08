package br.com.mariofronza.sigabot.usecases.misc.config.security

import br.com.mariofronza.sigabot.usecases.misc.filter.JWTAuthenticationFilter
import br.com.mariofronza.sigabot.usecases.misc.filter.JWTAuthorizationFilter
import br.com.mariofronza.sigabot.usecases.misc.utils.JWTUtil
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.GET
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
    private val jwtUtil: JWTUtil
) {

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(): AuthenticationManager = ProviderManager(
        DaoAuthenticationProvider(userDetailsService).apply { setPasswordEncoder(bCryptPasswordEncoder()) }
    )

    @Bean
    fun securityFilterChain(http: HttpSecurity, authenticationManager: AuthenticationManager): SecurityFilterChain {
        val authenticationFilter = JWTAuthenticationFilter(authenticationManager, jwtUtil)
            .apply { setFilterProcessesUrl("/sessions") }

        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/sessions/**", "/users/**", "/health/**", "/auth/**", "/error").permitAll()
                    .requestMatchers(GET, "/retrieve/**", "/dashboard/**").hasAuthority("STUDENT")
                    .requestMatchers(POST, "/notifications/**").hasAuthority("ADMIN")
                    .anyRequest().authenticated()
            }
            .addFilter(authenticationFilter)
            .addFilterBefore(
                JWTAuthorizationFilter(authenticationManager, jwtUtil, userDetailsService),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .build()
    }
}
