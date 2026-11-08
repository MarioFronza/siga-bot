package br.com.mariofronza.sigabot.persistence.user.repository

import br.com.mariofronza.sigabot.persistence.user.entity.UserJPA
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserJPA, Long> {

    fun findByUsername(username: String): UserJPA

    fun findByIsActiveTrue(): List<UserJPA>

    fun findByEmail(email: String): UserJPA?
}
