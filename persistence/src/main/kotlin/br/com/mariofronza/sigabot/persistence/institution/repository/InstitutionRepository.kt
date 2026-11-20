package br.com.mariofronza.sigabot.persistence.institution.repository

import br.com.mariofronza.sigabot.persistence.institution.entity.InstitutionJPA
import org.springframework.data.jpa.repository.JpaRepository

interface InstitutionRepository : JpaRepository<InstitutionJPA, Long> {

    fun findByCode(code: String): InstitutionJPA?
}
