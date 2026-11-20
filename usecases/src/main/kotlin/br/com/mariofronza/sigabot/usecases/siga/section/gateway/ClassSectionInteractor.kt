package br.com.mariofronza.sigabot.usecases.siga.section.gateway

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.term.AcademicTerm

interface ClassSectionInteractor {

    fun find(name: String, course: Course, period: AcademicTerm): ClassSection?

    fun save(group: ClassSection): ClassSection
}
