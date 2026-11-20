package br.com.mariofronza.sigabot.persistence.section.gateway

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.CLASS_SECTIONS
import br.com.mariofronza.sigabot.usecases.siga.section.gateway.ClassSectionInteractor
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class ClassSectionRepositoryInteractor(
    private val dsl: DSLContext,
) : ClassSectionInteractor {

    override fun find(code: String, course: Course, academicTerm: AcademicTerm): ClassSection? =
        dsl.select(CLASS_SECTIONS.ID)
            .from(CLASS_SECTIONS)
            .where(
                CLASS_SECTIONS.CODE.eq(code)
                    .and(CLASS_SECTIONS.COURSE_ID.eq(course.id))
                    .and(CLASS_SECTIONS.ACADEMIC_TERM_ID.eq(academicTerm.id))
            )
            .fetchOne { ClassSection(id = it.value1(), code = code, course = course, academicTerm = academicTerm) }

    override fun save(classSection: ClassSection): ClassSection {
        val id = dsl.insertInto(CLASS_SECTIONS)
            .set(CLASS_SECTIONS.ACADEMIC_TERM_ID, classSection.academicTerm.id)
            .set(CLASS_SECTIONS.COURSE_ID, classSection.course.id)
            .set(CLASS_SECTIONS.CODE, classSection.code)
            .returningResult(CLASS_SECTIONS.ID)
            .fetchSingle { it.value1() }
        return classSection.copy(id = id)
    }
}
