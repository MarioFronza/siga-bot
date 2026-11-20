package br.com.mariofronza.sigabot.usecases.siga.term.dto

import br.com.mariofronza.sigabot.entity.term.AcademicTerm

data class AcademicTermOutput(
    val year: Int,
    val term: Int,
) {
    companion object {
        fun from(academicTerm: AcademicTerm) = AcademicTermOutput(year = academicTerm.period.year, term = academicTerm.period.term)
    }
}
