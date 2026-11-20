package br.com.mariofronza.sigabot.persistence.term.gateway

import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.term.entity.AcademicTermJPA
import br.com.mariofronza.sigabot.persistence.term.repository.AcademicTermRepository
import br.com.mariofronza.sigabot.usecases.siga.term.gateway.AcademicTermInteractor
import org.springframework.stereotype.Component

@Component
class AcademicTermRepositoryInteractor(
    private val academicTermRepository: AcademicTermRepository,
) : AcademicTermInteractor {

    override fun findByYearAndTerm(year: Int, term: Int): AcademicTerm? =
        academicTermRepository.findByYearAndTerm(year, term)?.toEntity()

    override fun findAllEnrolledBy(user: User): List<AcademicTerm> =
        academicTermRepository.findAllEnrolledBy(user.id!!).map { it.toEntity() }

    override fun save(academicTerm: AcademicTerm): AcademicTerm =
        academicTermRepository.save(AcademicTermJPA(year = academicTerm.year, term = academicTerm.term)).toEntity()
}
