package br.com.mariofronza.sigabot.usecases.user.dto

import br.com.mariofronza.sigabot.entity.group.Period

data class UserPeriodsOutput(
    val username: String,
    val periods: Set<Period>
)
