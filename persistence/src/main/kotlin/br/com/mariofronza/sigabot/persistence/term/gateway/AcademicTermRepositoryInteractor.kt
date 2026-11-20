package br.com.mariofronza.sigabot.persistence.term.gateway

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.institution.repository.InstitutionRepository
import br.com.mariofronza.sigabot.persistence.term.entity.AcademicTermJPA
import br.com.mariofronza.sigabot.persistence.term.repository.AcademicTermRepository
import br.com.mariofronza.sigabot.usecases.siga.term.gateway.AcademicTermInteractor
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class AcademicTermRepositoryInteractor(
    private val academicTermRepository: AcademicTermRepository,
    private val institutionRepository: InstitutionRepository,
) : AcademicTermInteractor {

    override fun findByYearAndTerm(institution: Institution, year: Int, term: Int): AcademicTerm? =
        academicTermRepository.findByInstitutionIdAndYearAndTerm(institution.id!!, year, term)
            ?.let { AcademicTerm(id = it.id, institution = institution, year = year, term = term) }

    override fun findAllEnrolledBy(user: User): List<AcademicTerm> =
        academicTermRepository.findAllEnrolledBy(user.id!!).map { it.toEntity() }

    override fun save(academicTerm: AcademicTerm): AcademicTerm {
        val saved = academicTermRepository.save(
            AcademicTermJPA(
                institution = institutionRepository.getReferenceById(academicTerm.institution.id!!),
                year = academicTerm.year,
                term = academicTerm.term,
            )
        )
        return academicTerm.copy(id = saved.id)
    }
}
