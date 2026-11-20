package br.com.mariofronza.sigabot.usecases.siga.term

import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.usecases.siga.term.gateway.AcademicTermInteractor
import org.springframework.stereotype.Service

@Service
class AcademicTermService(
    private val academicTermInteractor: AcademicTermInteractor
) {

    fun findByYearAndTerm(year: String, term: Int): AcademicTerm {
        return academicTermInteractor.findByYearAndTerm(year, term) ?: throw Exception("Academic term not found")
    }

    fun createPeriod(year: String, term: Int): AcademicTerm {
        var period = academicTermInteractor.findByYearAndTerm(year, term)

        if (period == null) {
            period = academicTermInteractor.save(
                AcademicTerm(
                    year = year,
                    term = term
                )
            )
        }

        return period
    }
}
