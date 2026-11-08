package br.com.mariofronza.sigabot.usecases.siga.course

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.usecases.siga.course.gateway.CourseInteractor
import org.springframework.stereotype.Service

@Service
class CourseService(
    private val courseInteractor: CourseInteractor
) {

    fun createCourse(courseName: String): Course {
        var course = courseInteractor.findByName(courseName)

        if (course == null) {
            course = courseInteractor.save(
                Course(name = courseName)
            )
        }

        return course
    }
}
