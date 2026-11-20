package br.com.mariofronza.sigabot.infrastructure.crypto

import br.com.mariofronza.sigabot.usecases.credential.gateway.PasswordHasher
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordHasher : PasswordHasher {

    private val encoder = BCryptPasswordEncoder()

    override fun hash(rawPassword: String): String =
        checkNotNull(encoder.encode(rawPassword)) { "BCrypt returned no hash" }
}
