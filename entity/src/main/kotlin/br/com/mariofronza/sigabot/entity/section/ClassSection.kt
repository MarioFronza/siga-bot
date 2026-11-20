package br.com.mariofronza.sigabot.entity.section

import br.com.mariofronza.sigabot.entity.term.AcademicTerm

import br.com.mariofronza.sigabot.entity.course.Course

data class ClassSection(
    val id: Long? = null,
    val name: String,
    val course: Course,
    val period: AcademicTerm,
)
