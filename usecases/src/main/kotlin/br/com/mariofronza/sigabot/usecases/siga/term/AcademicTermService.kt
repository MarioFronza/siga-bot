package br.com.mariofronza.sigabot.usecases.siga.term

import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.siga.term.gateway.AcademicTermInteractor
import org.springframework.stereotype.Service

@Service
class AcademicTermService(
    private val academicTermInteractor: AcademicTermInteractor
) {

    fun findAllEnrolledBy(user: User): List<AcademicTerm> = academicTermInteractor.findAllEnrolledBy(user)

    fun findOrCreate(institution: Institution, period: AcademicPeriod): AcademicTerm =
        academicTermInteractor.findByPeriod(institution, period)
            ?: academicTermInteractor.save(AcademicTerm(institution = institution, period = period))
}
