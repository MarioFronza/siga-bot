package br.com.mariofronza.sigabot.persistence.course.gateway

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.persistence.course.entity.CourseJPA
import br.com.mariofronza.sigabot.persistence.course.repository.CourseRepository
import br.com.mariofronza.sigabot.persistence.institution.repository.InstitutionRepository
import br.com.mariofronza.sigabot.usecases.siga.course.gateway.CourseInteractor
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class CourseRepositoryInteractor(
    private val courseRepository: CourseRepository,
    private val institutionRepository: InstitutionRepository,
) : CourseInteractor {

    override fun findByName(institution: Institution, name: String): Course? =
        courseRepository.findByInstitutionIdAndName(institution.id!!, name)?.let { course -> Course(course.id, institution, name) }

    override fun save(course: Course): Course {
        val saved = courseRepository.save(
            CourseJPA(institution = institutionRepository.getReferenceById(course.institution.id!!), name = course.name)
        )
        return course.copy(id = saved.id)
    }
}
