package br.com.mariofronza.sigabot.usecases.siga.section

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.usecases.siga.section.gateway.ClassSectionInteractor
import org.springframework.stereotype.Service

@Service
class ClassSectionService(
    private val classSectionInteractor: ClassSectionInteractor
) {

    fun findOrCreate(code: String, course: Course, academicTerm: AcademicTerm): ClassSection {
        return classSectionInteractor.find(code, course, academicTerm)
            ?: classSectionInteractor.save(ClassSection(code = code, course = course, academicTerm = academicTerm))
    }
}
