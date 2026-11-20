package br.com.mariofronza.sigabot.usecases.siga.term.gateway

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.entity.user.User

interface AcademicTermInteractor {

    fun findByYearAndTerm(institution: Institution, year: Int, term: Int): AcademicTerm?

    fun findAllEnrolledBy(user: User): List<AcademicTerm>

    fun save(academicTerm: AcademicTerm): AcademicTerm
}
