package br.com.mariofronza.sigabot.usecases.siga.course

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.usecases.siga.course.gateway.CourseInteractor
import org.springframework.stereotype.Service

@Service
class CourseService(
    private val courseInteractor: CourseInteractor
) {

    fun findOrCreate(institution: Institution, name: String): Course {
        return courseInteractor.findByName(institution, name)
            ?: courseInteractor.save(Course(institution = institution, name = name))
    }
}
