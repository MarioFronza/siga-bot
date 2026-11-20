package br.com.mariofronza.sigabot.usecases.identity

import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import org.slf4j.LoggerFactory

class GrantAdmins(
    private val users: UserRepository,
    private val adminEmails: Set<Email>,
) {

    operator fun invoke(): Int = adminEmails.count { email ->
        val user = users.findByEmail(email)?.takeUnless { Role.ADMIN in it.roles } ?: return@count false
        users.save(user.grant(Role.ADMIN))
        logger.info("Granted ADMIN to user {}", user.id)
        true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GrantAdmins::class.java)
    }
}
