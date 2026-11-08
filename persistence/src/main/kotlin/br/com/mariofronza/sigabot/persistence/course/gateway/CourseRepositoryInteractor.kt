package br.com.mariofronza.sigabot.persistence.course.gateway

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.persistence.course.repository.CourseRepository
import br.com.mariofronza.sigabot.persistence.misc.toJPA
import br.com.mariofronza.sigabot.usecases.siga.course.gateway.CourseInteractor
import org.springframework.stereotype.Component

@Component
class CourseRepositoryInteractor(
    private val courseRepository: CourseRepository
) : CourseInteractor {

    override fun findByName(name: String): Course? {
        return courseRepository.findByName(name)?.toEntity()
    }

    override fun save(course: Course): Course {
        return courseRepository.save(course.toJPA()).toEntity()
    }
}
