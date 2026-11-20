package br.com.mariofronza.sigabot.persistence.user.repository

import br.com.mariofronza.sigabot.persistence.user.entity.UserJPA
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<UserJPA, Long> {

    fun findByEmail(email: String): UserJPA?

    fun findByActiveTrue(): List<UserJPA>

    fun findByExternalId(externalId: UUID): UserJPA?
}
