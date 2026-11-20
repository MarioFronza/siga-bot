package br.com.mariofronza.sigabot.persistence.term.repository

import br.com.mariofronza.sigabot.persistence.term.entity.AcademicTermJPA
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AcademicTermRepository : JpaRepository<AcademicTermJPA, Long> {

    fun findByYearAndTerm(year: String, term: Int): AcademicTermJPA?
}
