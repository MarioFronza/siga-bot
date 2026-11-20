package br.com.mariofronza.sigabot.persistence.subject.gateway

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.SUBJECTS
import br.com.mariofronza.sigabot.usecases.siga.subject.gateway.SubjectInteractor
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class SubjectRepositoryInteractor(
    private val dsl: DSLContext,
) : SubjectInteractor {

    override fun findByName(institution: Institution, name: String): Subject? =
        dsl.select(SUBJECTS.ID)
            .from(SUBJECTS)
            .where(SUBJECTS.INSTITUTION_ID.eq(institution.id).and(SUBJECTS.NAME.eq(name)))
            .fetchOne { Subject(id = it.value1(), institution = institution, name = name) }

    override fun save(subject: Subject): Subject {
        val id = dsl.insertInto(SUBJECTS)
            .set(SUBJECTS.INSTITUTION_ID, subject.institution.id)
            .set(SUBJECTS.NAME, subject.name)
            .returningResult(SUBJECTS.ID)
            .fetchSingle { it.value1() }
        return subject.copy(id = id)
    }
}
