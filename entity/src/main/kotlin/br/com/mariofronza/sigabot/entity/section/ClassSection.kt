package br.com.mariofronza.sigabot.entity.section

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.term.AcademicTerm

data class ClassSection(
    val id: Long? = null,
    val code: String,
    val course: Course,
    val academicTerm: AcademicTerm,
)
