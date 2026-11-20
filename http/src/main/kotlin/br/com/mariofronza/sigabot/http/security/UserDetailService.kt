package br.com.mariofronza.sigabot.http.security

import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailService(
    val userInteractor: UserInteractor
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = runCatching { Email.of(email) }.getOrNull()?.let(userInteractor::findByEmail)
            ?: throw UsernameNotFoundException("User not found")
        return UserDetailsImpl(user)
    }
}
