package br.com.mariofronza.sigabot.usecases.retrieve.dto

import br.com.mariofronza.sigabot.usecases.siga.term.dto.AcademicTermOutput

data class RetrieveOutput(
    val username: String,
    val periods: List<AcademicTermOutput>
)
