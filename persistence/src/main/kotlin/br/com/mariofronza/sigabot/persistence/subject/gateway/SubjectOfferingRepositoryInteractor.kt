package br.com.mariofronza.sigabot.persistence.subject.gateway

import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.SUBJECT_OFFERINGS
import br.com.mariofronza.sigabot.usecases.siga.subject.gateway.SubjectOfferingInteractor
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class SubjectOfferingRepositoryInteractor(
    private val dsl: DSLContext,
) : SubjectOfferingInteractor {

    override fun find(subject: Subject, classSection: ClassSection): SubjectOffering? =
        dsl.selectFrom(SUBJECT_OFFERINGS)
            .where(SUBJECT_OFFERINGS.SUBJECT_ID.eq(subject.id).and(SUBJECT_OFFERINGS.CLASS_SECTION_ID.eq(classSection.id)))
            .fetchOne { SubjectOffering(id = it.id, subject = subject, classSection = classSection, courseLoad = it.courseLoad) }

    override fun save(subjectOffering: SubjectOffering): SubjectOffering {
        val id = dsl.insertInto(SUBJECT_OFFERINGS)
            .set(SUBJECT_OFFERINGS.CLASS_SECTION_ID, subjectOffering.classSection.id)
            .set(SUBJECT_OFFERINGS.SUBJECT_ID, subjectOffering.subject.id)
            .set(SUBJECT_OFFERINGS.COURSE_LOAD, subjectOffering.courseLoad)
            .returningResult(SUBJECT_OFFERINGS.ID)
            .fetchSingle { it.value1() }
        return subjectOffering.copy(id = id)
    }
}
