package br.com.mariofronza.sigabot.persistence.user.gateway

import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.ROLES
import br.com.mariofronza.sigabot.usecases.user.gateway.RoleInteractor
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class RoleRepositoryInteractor(
    private val dsl: DSLContext,
) : RoleInteractor {

    override fun findByName(name: String): Role? =
        dsl.selectFrom(ROLES).where(ROLES.NAME.eq(name)).fetchOne()?.let { Role(id = it.id, name = it.name) }
}
