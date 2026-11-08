package br.com.mariofronza.sigabot.persistence.user.repository

import br.com.mariofronza.sigabot.persistence.user.entity.RoleJPA
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<RoleJPA, Long> {

    fun findByName(name: String): RoleJPA?
}
