package br.com.mariofronza.sigabot.persistence.group.repository

import br.com.mariofronza.sigabot.persistence.group.entity.PeriodJPA
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PeriodRepository : JpaRepository<PeriodJPA, Long> {

    fun findByYearAndTerm(year: String, term: Int): PeriodJPA?
}
