package br.com.mariofronza.sigabot.entity.course

import br.com.mariofronza.sigabot.entity.institution.Institution

data class Course(
    val id: Long? = null,
    val institution: Institution,
    val name: String,
)
