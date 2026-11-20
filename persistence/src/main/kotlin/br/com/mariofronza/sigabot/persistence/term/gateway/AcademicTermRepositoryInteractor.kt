package br.com.mariofronza.sigabot.persistence.term.gateway

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.institution.gateway.toInstitution
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.ACADEMIC_TERMS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.CLASS_SECTIONS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.ENROLLMENTS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.INSTITUTIONS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.SUBJECT_OFFERINGS
import br.com.mariofronza.sigabot.usecases.siga.term.gateway.AcademicTermInteractor
import org.jooq.DSLContext
import org.jooq.impl.DSL.select
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class AcademicTermRepositoryInteractor(
    private val dsl: DSLContext,
) : AcademicTermInteractor {

    override fun findByYearAndTerm(institution: Institution, year: Int, term: Int): AcademicTerm? =
        dsl.select(ACADEMIC_TERMS.ID)
            .from(ACADEMIC_TERMS)
            .where(
                ACADEMIC_TERMS.INSTITUTION_ID.eq(institution.id)
                    .and(ACADEMIC_TERMS.YEAR.eq(year))
                    .and(ACADEMIC_TERMS.TERM.eq(term))
            )
            .fetchOne { AcademicTerm(id = it.value1(), institution = institution, year = year, term = term) }

    override fun findAllEnrolledBy(user: User): List<AcademicTerm> =
        dsl.select(ACADEMIC_TERMS, INSTITUTIONS)
            .from(ACADEMIC_TERMS)
            .join(INSTITUTIONS).on(INSTITUTIONS.ID.eq(ACADEMIC_TERMS.INSTITUTION_ID))
            .where(
                ACADEMIC_TERMS.ID.`in`(
                    select(CLASS_SECTIONS.ACADEMIC_TERM_ID)
                        .from(ENROLLMENTS)
                        .join(SUBJECT_OFFERINGS).on(SUBJECT_OFFERINGS.ID.eq(ENROLLMENTS.SUBJECT_OFFERING_ID))
                        .join(CLASS_SECTIONS).on(CLASS_SECTIONS.ID.eq(SUBJECT_OFFERINGS.CLASS_SECTION_ID))
                        .where(ENROLLMENTS.USER_ID.eq(user.id))
                )
            )
            .orderBy(ACADEMIC_TERMS.YEAR.desc(), ACADEMIC_TERMS.TERM.desc())
            .fetch { (term, institution) ->
                AcademicTerm(id = term.id, institution = institution.toInstitution(), year = term.year, term = term.term)
            }

    override fun save(academicTerm: AcademicTerm): AcademicTerm {
        val id = dsl.insertInto(ACADEMIC_TERMS)
            .set(ACADEMIC_TERMS.INSTITUTION_ID, academicTerm.institution.id)
            .set(ACADEMIC_TERMS.YEAR, academicTerm.year)
            .set(ACADEMIC_TERMS.TERM, academicTerm.term)
            .returningResult(ACADEMIC_TERMS.ID)
            .fetchSingle { it.value1() }
        return academicTerm.copy(id = id)
    }
}
