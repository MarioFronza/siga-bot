package br.com.mariofronza.sigabot.usecases.siga.group

import br.com.mariofronza.sigabot.entity.group.Period
import br.com.mariofronza.sigabot.usecases.siga.group.gateway.PeriodInteractor
import org.springframework.stereotype.Service

@Service
class PeriodService(
    private val periodInteractor: PeriodInteractor
) {

    fun findByYearAndTerm(year: String, term: Int): Period {
        return periodInteractor.findByYearAndTerm(year, term) ?: throw Exception("Period not found")
    }

    fun createPeriod(year: String, term: Int): Period {
        var period = periodInteractor.findByYearAndTerm(year, term)

        if (period == null) {
            period = periodInteractor.save(
                Period(
                    year = year,
                    term = term
                )
            )
        }

        return period
    }
}
