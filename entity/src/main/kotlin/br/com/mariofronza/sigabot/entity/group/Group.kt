package br.com.mariofronza.sigabot.entity.group

import br.com.mariofronza.sigabot.entity.course.Course

data class Group(
    val id: Long? = null,
    val name: String,
    val course: Course,
    val period: Period,
)
