package br.com.mariofronza.sigabot.usecases.user.gateway

import br.com.mariofronza.sigabot.entity.user.User
import java.util.UUID

interface UserInteractor {

    fun findAllActive(): List<User>

    fun findByExternalId(externalId: UUID): User?

    fun findByEmail(email: String): User?

    fun save(user: User): User
}
