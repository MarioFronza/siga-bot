package br.com.mariofronza.sigabot.usecases.siga.term.gateway

import br.com.mariofronza.sigabot.entity.term.AcademicTerm

interface AcademicTermInteractor {

    fun findByYearAndTerm(year: String, term: Int): AcademicTerm?

    fun save(period: AcademicTerm): AcademicTerm
}
