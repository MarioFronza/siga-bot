package br.com.mariofronza.sigabot.persistence.section.gateway

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.persistence.course.repository.CourseRepository
import br.com.mariofronza.sigabot.persistence.section.entity.ClassSectionJPA
import br.com.mariofronza.sigabot.persistence.section.repository.ClassSectionRepository
import br.com.mariofronza.sigabot.persistence.term.repository.AcademicTermRepository
import br.com.mariofronza.sigabot.usecases.siga.section.gateway.ClassSectionInteractor
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class ClassSectionRepositoryInteractor(
    private val classSectionRepository: ClassSectionRepository,
    private val courseRepository: CourseRepository,
    private val academicTermRepository: AcademicTermRepository,
) : ClassSectionInteractor {

    override fun find(code: String, course: Course, academicTerm: AcademicTerm): ClassSection? =
        classSectionRepository.findByCodeAndCourseIdAndAcademicTermId(code, course.id!!, academicTerm.id!!)
            ?.let { ClassSection(id = it.id, code = code, course = course, academicTerm = academicTerm) }

    override fun save(classSection: ClassSection): ClassSection {
        val saved = classSectionRepository.save(
            ClassSectionJPA(
                academicTerm = academicTermRepository.getReferenceById(classSection.academicTerm.id!!),
                course = courseRepository.getReferenceById(classSection.course.id!!),
                code = classSection.code,
            )
        )
        return classSection.copy(id = saved.id)
    }
}
