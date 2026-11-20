package br.com.mariofronza.sigabot.persistence.institution.gateway

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.INSTITUTIONS
import br.com.mariofronza.sigabot.usecases.institution.gateway.InstitutionInteractor
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.select
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

internal fun institutionKey(code: InstitutionCode): Field<Long?> =
    field(select(INSTITUTIONS.ID).from(INSTITUTIONS).where(INSTITUTIONS.CODE.eq(code.value)))

@Component
@Transactional
class InstitutionRepositoryInteractor(
    private val dsl: DSLContext,
) : InstitutionInteractor {

    override fun findByCode(code: InstitutionCode): Institution? =
        dsl.selectFrom(INSTITUTIONS)
            .where(INSTITUTIONS.CODE.eq(code.value))
            .fetchOne { Institution(code = InstitutionCode(it.code), name = it.name, gradeScaleMax = it.gradeScaleMax.toDouble()) }
}
