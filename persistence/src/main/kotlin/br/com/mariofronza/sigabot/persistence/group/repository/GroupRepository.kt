package br.com.mariofronza.sigabot.persistence.group.repository

import br.com.mariofronza.sigabot.persistence.group.entity.GroupJPA
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GroupRepository : JpaRepository<GroupJPA, Long> {

    fun findByName(name: String): GroupJPA?
}
