package br.com.mariofronza.sigabot.usecases.siga.course.gateway

import br.com.mariofronza.sigabot.entity.course.Course

interface CourseInteractor {

    fun findByName(name: String): Course?

    fun save(course: Course): Course
}
