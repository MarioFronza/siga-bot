package br.com.mariofronza.sigabot.entity.subject

import br.com.mariofronza.sigabot.entity.institution.Institution

data class Subject(
    val id: Long? = null,
    val institution: Institution,
    val name: String,
)
