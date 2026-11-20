package br.com.mariofronza.sigabot.persistence.term.gateway

import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.persistence.term.repository.AcademicTermRepository
import br.com.mariofronza.sigabot.persistence.misc.toJPA
import br.com.mariofronza.sigabot.usecases.siga.term.gateway.AcademicTermInteractor
import org.springframework.stereotype.Component

@Component
class AcademicTermRepositoryInteractor(
    private val academicTermRepository: AcademicTermRepository
) : AcademicTermInteractor {

    override fun findByYearAndTerm(year: String, term: Int): AcademicTerm? {
        return academicTermRepository.findByYearAndTerm(year, term)?.toEntity()
    }

    override fun save(period: AcademicTerm): AcademicTerm {
        return academicTermRepository.save(period.toJPA()).toEntity()
    }
}
