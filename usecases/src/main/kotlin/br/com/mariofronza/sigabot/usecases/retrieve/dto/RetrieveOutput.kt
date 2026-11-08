package br.com.mariofronza.sigabot.usecases.retrieve.dto

import br.com.mariofronza.sigabot.entity.group.Period

data class RetrieveOutput(
    val username: String,
    val periods: List<Period>
)
