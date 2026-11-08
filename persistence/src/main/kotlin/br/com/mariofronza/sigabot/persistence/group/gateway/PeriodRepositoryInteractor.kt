package br.com.mariofronza.sigabot.persistence.group.gateway

import br.com.mariofronza.sigabot.entity.group.Period
import br.com.mariofronza.sigabot.persistence.group.repository.PeriodRepository
import br.com.mariofronza.sigabot.persistence.misc.toJPA
import br.com.mariofronza.sigabot.usecases.siga.group.gateway.PeriodInteractor
import org.springframework.stereotype.Component

@Component
class PeriodRepositoryInteractor(
    private val periodRepository: PeriodRepository
) : PeriodInteractor {

    override fun findByYearAndTerm(year: String, term: Int): Period? {
        return periodRepository.findByYearAndTerm(year, term)?.toEntity()
    }

    override fun save(period: Period): Period {
        return periodRepository.save(period.toJPA()).toEntity()
    }
}
