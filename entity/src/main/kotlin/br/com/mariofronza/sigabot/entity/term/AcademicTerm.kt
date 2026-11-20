package br.com.mariofronza.sigabot.entity.term

import br.com.mariofronza.sigabot.entity.institution.Institution

data class AcademicTerm(
    val id: Long? = null,
    val institution: Institution,
    val year: Int,
    val term: Int,
)
