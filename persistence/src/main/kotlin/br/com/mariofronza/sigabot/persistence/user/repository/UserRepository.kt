package br.com.mariofronza.sigabot.persistence.user.repository

import br.com.mariofronza.sigabot.persistence.user.entity.UserJPA
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserJPA, Long> {

    fun findByEmail(email: String): UserJPA?

    fun findByActiveTrue(): List<UserJPA>
}
