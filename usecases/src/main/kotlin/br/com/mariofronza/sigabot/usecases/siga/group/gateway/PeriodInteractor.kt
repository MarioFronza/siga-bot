package br.com.mariofronza.sigabot.usecases.siga.group.gateway

import br.com.mariofronza.sigabot.entity.group.Period

interface PeriodInteractor {

    fun findByYearAndTerm(year: String, term: Int): Period?

    fun save(period: Period): Period
}
