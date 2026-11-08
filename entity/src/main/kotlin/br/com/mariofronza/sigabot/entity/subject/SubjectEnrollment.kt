package br.com.mariofronza.sigabot.entity.subject

import br.com.mariofronza.sigabot.entity.group.Group

class SubjectEnrollment(
    val id: Long? = null,
    val subject: Subject,
    val enrolledGroup: Group
)
