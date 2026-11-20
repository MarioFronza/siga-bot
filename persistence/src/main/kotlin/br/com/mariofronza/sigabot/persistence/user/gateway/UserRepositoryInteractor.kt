package br.com.mariofronza.sigabot.persistence.user.gateway

import br.com.mariofronza.sigabot.persistence.notNull
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.institution.gateway.toInstitution
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.ACADEMIC_ACCOUNTS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.INSTITUTIONS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.ROLES
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.USERS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.USER_ROLES
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL.currentOffsetDateTime
import org.jooq.impl.DSL.multiset
import org.jooq.impl.DSL.select
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
@Transactional
class UserRepositoryInteractor(
    private val dsl: DSLContext,
) : UserInteractor {

    override fun findAllActive(): List<User> = fetch(USERS.ACTIVE.isTrue)

    override fun findByExternalId(externalId: UUID): User? = fetch(USERS.EXTERNAL_ID.eq(externalId)).singleOrNull()

    override fun findByEmail(email: String): User? = fetch(USERS.EMAIL.eq(email)).singleOrNull()

    override fun save(user: User): User = user.id?.let { update(user) } ?: insert(user)

    private fun fetch(condition: Condition): List<User> =
        dsl.select(
            USERS,
            multiset(
                select(ROLES)
                    .from(USER_ROLES)
                    .join(ROLES).on(ROLES.ID.eq(USER_ROLES.ROLE_ID))
                    .where(USER_ROLES.USER_ID.eq(USERS.ID))
            ).convertFrom { roles -> roles.map { (role) -> Role(id = role.id, name = role.name) } },
            multiset(
                select(ACADEMIC_ACCOUNTS, INSTITUTIONS)
                    .from(ACADEMIC_ACCOUNTS)
                    .join(INSTITUTIONS).on(INSTITUTIONS.ID.eq(ACADEMIC_ACCOUNTS.INSTITUTION_ID))
                    .where(ACADEMIC_ACCOUNTS.USER_ID.eq(USERS.ID))
            ).convertFrom { accounts ->
                accounts.map { (account, institution) ->
                    AcademicAccount(
                        institution = institution.toInstitution(),
                        loginCiphertext = account.loginCiphertext,
                        loginDigest = account.loginDigest,
                        passwordCiphertext = account.passwordCiphertext,
                    )
                }
            },
        )
            .from(USERS)
            .where(condition)
            .orderBy(USERS.ID)
            .fetch { (row, roles, accounts) ->
                User(
                    id = row.id,
                    externalId = row.externalId,
                    isActive = row.notNull(USERS.ACTIVE),
                    fullName = row.notNull(USERS.FULL_NAME),
                    email = row.email,
                    passwordHash = row.passwordHash,
                    roles = roles,
                    academicAccounts = accounts,
                )
            }

    private fun update(user: User): User {
        dsl.update(USERS)
            .set(USERS.EMAIL, user.email)
            .set(USERS.PASSWORD_HASH, user.passwordHash)
            .set(USERS.FULL_NAME, user.fullName)
            .set(USERS.ACTIVE, user.isActive)
            .set(USERS.UPDATED_AT, currentOffsetDateTime())
            .where(USERS.ID.eq(user.id))
            .execute()
        return user
    }

    private fun insert(user: User): User {
        val (id, externalId) = dsl.insertInto(USERS)
            .set(USERS.EMAIL, user.email)
            .set(USERS.PASSWORD_HASH, user.passwordHash)
            .set(USERS.FULL_NAME, user.fullName)
            .set(USERS.ACTIVE, user.isActive)
            .returningResult(USERS.ID, USERS.EXTERNAL_ID)
            .fetchSingle()
        user.roles.forEach {
            dsl.insertInto(USER_ROLES).set(USER_ROLES.USER_ID, id).set(USER_ROLES.ROLE_ID, it.id).execute()
        }
        user.academicAccounts.forEach {
            dsl.insertInto(ACADEMIC_ACCOUNTS)
                .set(ACADEMIC_ACCOUNTS.USER_ID, id)
                .set(ACADEMIC_ACCOUNTS.INSTITUTION_ID, it.institution.id)
                .set(ACADEMIC_ACCOUNTS.LOGIN_CIPHERTEXT, it.loginCiphertext)
                .set(ACADEMIC_ACCOUNTS.LOGIN_DIGEST, it.loginDigest)
                .set(ACADEMIC_ACCOUNTS.PASSWORD_CIPHERTEXT, it.passwordCiphertext)
                .execute()
        }
        return user.copy(id = id, externalId = externalId)
    }
}
