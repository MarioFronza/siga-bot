package br.com.mariofronza.sigabot.usecases.user.gateway

import br.com.mariofronza.sigabot.entity.user.User

interface UserInteractor {

    fun findAllActive(): List<User>

    fun findById(id: Long): User?

    fun findByEmail(email: String): User?

    fun save(user: User): User
}
