package br.com.mariofronza.sigabot.usecases.user.gateway

import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.UserId

interface UserInteractor {

    fun findAllActive(): List<User>

    fun findById(id: UserId): User?

    fun findByEmail(email: Email): User?

    fun save(user: User): User
}
