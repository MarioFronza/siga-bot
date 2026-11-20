package br.com.mariofronza.sigabot.usecases.user.dto

import br.com.mariofronza.sigabot.entity.term.AcademicTerm

data class UserPeriodsOutput(
    val username: String,
    val periods: Set<AcademicTerm>
)
