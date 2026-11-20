package br.com.mariofronza.sigabot.usecases.siga.term

import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.siga.term.gateway.AcademicTermInteractor
import org.springframework.stereotype.Service

@Service
class AcademicTermService(
    private val academicTermInteractor: AcademicTermInteractor
) {

    fun findByYearAndTerm(year: Int, term: Int): AcademicTerm {
        return academicTermInteractor.findByYearAndTerm(year, term) ?: throw Exception("Academic term not found")
    }

    fun findAllEnrolledBy(user: User): List<AcademicTerm> = academicTermInteractor.findAllEnrolledBy(user)

    fun findOrCreate(year: Int, term: Int): AcademicTerm {
        return academicTermInteractor.findByYearAndTerm(year, term)
            ?: academicTermInteractor.save(AcademicTerm(year = year, term = term))
    }
}
