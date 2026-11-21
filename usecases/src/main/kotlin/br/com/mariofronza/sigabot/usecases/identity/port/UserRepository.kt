package br.com.mariofronza.sigabot.usecases.identity.port

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.usecases.shared.Page
import br.com.mariofronza.sigabot.usecases.shared.PageRequest

interface UserRepository {

    fun findAllActive(): List<User>

    fun findById(id: UserId): User?

    fun findByEmail(email: Email): User?

    fun search(query: String, request: PageRequest): Page<User>

    fun existsByLoginDigest(institution: InstitutionCode, loginDigest: ByteArray): Boolean

    fun save(user: User): User
}
