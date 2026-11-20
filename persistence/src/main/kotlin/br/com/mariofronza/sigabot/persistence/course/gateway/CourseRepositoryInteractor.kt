package br.com.mariofronza.sigabot.persistence.course.gateway

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.COURSES
import br.com.mariofronza.sigabot.usecases.siga.course.gateway.CourseInteractor
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class CourseRepositoryInteractor(
    private val dsl: DSLContext,
) : CourseInteractor {

    override fun findByName(institution: Institution, name: String): Course? =
        dsl.select(COURSES.ID)
            .from(COURSES)
            .where(COURSES.INSTITUTION_ID.eq(institution.id).and(COURSES.NAME.eq(name)))
            .fetchOne { Course(id = it.value1(), institution = institution, name = name) }

    override fun save(course: Course): Course {
        val id = dsl.insertInto(COURSES)
            .set(COURSES.INSTITUTION_ID, course.institution.id)
            .set(COURSES.NAME, course.name)
            .returningResult(COURSES.ID)
            .fetchSingle { it.value1() }
        return course.copy(id = id)
    }
}
