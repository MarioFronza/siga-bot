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

    fun createGroup(groupName: String, course: Course, period: AcademicTerm): ClassSection {
        var group = classSectionInteractor.find(groupName, course, period)

        if (group == null) {
            group = classSectionInteractor.save(
                ClassSection(
                    name = groupName,
                    course = course,
                    period = period
                )
            )
        }

        return group
    }
}
