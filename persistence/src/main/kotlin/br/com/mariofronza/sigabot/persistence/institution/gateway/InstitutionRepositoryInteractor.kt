package br.com.mariofronza.sigabot.persistence.institution.gateway

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.persistence.jooq.tables.records.InstitutionsRecord
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.INSTITUTIONS
import br.com.mariofronza.sigabot.usecases.institution.gateway.InstitutionInteractor
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class InstitutionRepositoryInteractor(
    private val dsl: DSLContext,
) : InstitutionInteractor {

    override fun findByCode(code: String): Institution? =
        dsl.selectFrom(INSTITUTIONS).where(INSTITUTIONS.CODE.eq(code)).fetchOne()?.toInstitution()
}

internal fun InstitutionsRecord.toInstitution() =
    Institution(id = id, code = code, name = name, gradeScaleMax = gradeScaleMax.toDouble())
