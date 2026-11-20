package br.com.mariofronza.sigabot.usecases.siga.term.dto

import br.com.mariofronza.sigabot.entity.term.AcademicPeriod

data class AcademicTermOutput(
    val year: Int,
    val term: Int,
) {
    companion object {
        fun from(period: AcademicPeriod) = AcademicTermOutput(year = period.year, term = period.term)
    }
}
