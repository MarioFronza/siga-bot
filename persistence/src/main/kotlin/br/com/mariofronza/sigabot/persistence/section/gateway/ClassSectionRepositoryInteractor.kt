package br.com.mariofronza.sigabot.persistence.section.gateway

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.persistence.section.repository.ClassSectionRepository
import br.com.mariofronza.sigabot.persistence.misc.toJPA
import br.com.mariofronza.sigabot.usecases.siga.section.gateway.ClassSectionInteractor
import org.springframework.stereotype.Component

@Component
class ClassSectionRepositoryInteractor(
    private val classSectionRepository: ClassSectionRepository
) : ClassSectionInteractor {

    override fun find(name: String, course: Course, period: AcademicTerm): ClassSection? {
        return classSectionRepository.findByNameAndCourseIdAndPeriodId(name, course.id!!, period.id!!)?.toEntity()
    }

    override fun save(group: ClassSection): ClassSection {
        return classSectionRepository.save(group.toJPA()).toEntity()
    }
}
