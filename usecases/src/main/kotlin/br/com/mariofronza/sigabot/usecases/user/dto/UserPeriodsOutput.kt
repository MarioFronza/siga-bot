package br.com.mariofronza.sigabot.usecases.user.dto

import br.com.mariofronza.sigabot.usecases.siga.term.dto.AcademicTermOutput

data class UserPeriodsOutput(
    val username: String,
    val periods: List<AcademicTermOutput>
)
