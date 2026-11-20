package br.com.mariofronza.sigabot.entity.subject

import br.com.mariofronza.sigabot.entity.section.ClassSection

data class SubjectOffering(
    val id: Long? = null,
    val subject: Subject,
    val classSection: ClassSection,
    val courseLoad: Int,
)
