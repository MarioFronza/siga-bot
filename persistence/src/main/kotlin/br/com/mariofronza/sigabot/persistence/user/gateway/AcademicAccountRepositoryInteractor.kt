package br.com.mariofronza.sigabot.persistence.user.gateway

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.ACADEMIC_ACCOUNTS
import br.com.mariofronza.sigabot.usecases.user.gateway.AcademicAccountInteractor
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class AcademicAccountRepositoryInteractor(
    private val dsl: DSLContext,
) : AcademicAccountInteractor {

    override fun existsByLoginDigest(institution: Institution, loginDigest: ByteArray): Boolean =
        dsl.fetchExists(
            ACADEMIC_ACCOUNTS,
            ACADEMIC_ACCOUNTS.INSTITUTION_ID.eq(institution.id).and(ACADEMIC_ACCOUNTS.LOGIN_DIGEST.eq(loginDigest)),
        )
}
