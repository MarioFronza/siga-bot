package br.com.mariofronza.sigabot.usecases.user

import br.com.mariofronza.sigabot.usecases.misc.filter.UserDetailsImpl
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
        val user = userInteractor.findByEmail(email) ?: throw UsernameNotFoundException("User not found")
        return UserDetailsImpl(user)
    }
}
