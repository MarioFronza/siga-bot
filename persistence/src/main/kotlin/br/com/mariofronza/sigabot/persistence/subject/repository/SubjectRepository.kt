package br.com.mariofronza.sigabot.persistence.subject.repository

import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectJPA
import org.springframework.data.jpa.repository.JpaRepository

interface SubjectRepository : JpaRepository<SubjectJPA, Long> {

    fun findByName(name: String): SubjectJPA?
}
