package br.com.mariofronza.sigabot.entity.subject

import br.com.mariofronza.sigabot.entity.section.ClassSection

class SubjectOffering(
    val id: Long? = null,
    val subject: Subject,
    val enrolledGroup: ClassSection
)
