package br.com.mariofronza.sigabot.persistence.user

import org.jooq.impl.DSL.value
import org.jooq.impl.DSL.field
import org.jooq.Field
import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.persistence.notNull
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.persistence.institution.institutionKey
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.ACADEMIC_ACCOUNTS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.INSTITUTIONS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.ROLES
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.USERS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.USER_ROLES
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import br.com.mariofronza.sigabot.usecases.shared.Page
import br.com.mariofronza.sigabot.usecases.shared.PageRequest
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL.currentOffsetDateTime
import org.jooq.impl.DSL.multiset
import org.jooq.impl.DSL.select
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

internal fun userKey(id: UserId): Field<Long?> =
    field(select(USERS.ID).from(USERS).where(USERS.EXTERNAL_ID.eq(id.value)))

@Component
@Transactional
class JooqUserRepository(
    private val dsl: DSLContext,
) : UserRepository {

    override fun findAllActive(): List<User> = fetch(USERS.ACTIVE.isTrue)

    override fun findById(id: UserId): User? = fetch(USERS.EXTERNAL_ID.eq(id.value)).singleOrNull()

    override fun findByEmail(email: Email): User? = fetch(USERS.EMAIL.eq(email.value)).singleOrNull()

    override fun search(query: String, request: PageRequest): Page<User> {
        val matches = USERS.EMAIL.containsIgnoreCase(query).or(USERS.FULL_NAME.containsIgnoreCase(query))
        return Page(fetch(matches, request), request, dsl.fetchCount(USERS, matches).toLong())
    }

    override fun existsByLoginDigest(institution: InstitutionCode, loginDigest: ByteArray): Boolean =
        dsl.fetchExists(
            ACADEMIC_ACCOUNTS,
            ACADEMIC_ACCOUNTS.INSTITUTION_ID.eq(institutionKey(institution)).and(ACADEMIC_ACCOUNTS.LOGIN_DIGEST.eq(loginDigest)),
        )

    override fun save(user: User): User {
        if (dsl.fetchExists(USERS, USERS.EXTERNAL_ID.eq(user.id.value))) update(user) else insert(user)
        return user
    }

    private fun fetch(condition: Condition, page: PageRequest? = null): List<User> =
        dsl.select(
            USERS,
            multiset(
                select(ROLES)
                    .from(USER_ROLES)
                    .join(ROLES).on(ROLES.ID.eq(USER_ROLES.ROLE_ID))
                    .where(USER_ROLES.USER_ID.eq(USERS.ID))
            ).convertFrom { roles -> roles.map { (role) -> Role.valueOf(role.name) }.toSet() },
            multiset(
                select(ACADEMIC_ACCOUNTS, INSTITUTIONS)
                    .from(ACADEMIC_ACCOUNTS)
                    .join(INSTITUTIONS).on(INSTITUTIONS.ID.eq(ACADEMIC_ACCOUNTS.INSTITUTION_ID))
                    .where(ACADEMIC_ACCOUNTS.USER_ID.eq(USERS.ID))
            ).convertFrom { accounts ->
                accounts.map { (account, institution) ->
                    AcademicAccount(
                        institution = InstitutionCode(institution.code),
                        loginCiphertext = account.loginCiphertext,
                        loginDigest = account.loginDigest,
                        passwordCiphertext = account.passwordCiphertext,
                    )
                }
            },
        )
            .from(USERS)
            .where(condition)
            .orderBy(USERS.FULL_NAME, USERS.EMAIL)
            .limit(page?.size ?: Int.MAX_VALUE)
            .offset(page?.offset ?: 0)
            .fetch { (row, roles, accounts) ->
                User(
                    id = UserId(row.notNull(USERS.EXTERNAL_ID)),
                    isActive = row.notNull(USERS.ACTIVE),
                    fullName = row.notNull(USERS.FULL_NAME),
                    email = Email.of(row.email),
                    passwordHash = row.passwordHash,
                    roles = roles,
                    academicAccounts = accounts,
                )
            }

    private fun update(user: User) {
        dsl.update(USERS)
            .set(USERS.EMAIL, user.email.value)
            .set(USERS.PASSWORD_HASH, user.passwordHash)
            .set(USERS.FULL_NAME, user.fullName)
            .set(USERS.ACTIVE, user.isActive)
            .set(USERS.UPDATED_AT, currentOffsetDateTime())
            .where(USERS.EXTERNAL_ID.eq(user.id.value))
            .execute()
        dsl.deleteFrom(USER_ROLES).where(USER_ROLES.USER_ID.eq(userKey(user.id))).execute()
        insertRoles(userKey(user.id), user.roles)
    }

    private fun insertRoles(userId: Field<Long?>, roles: Set<Role>) {
        roles.forEach {
            dsl.insertInto(USER_ROLES)
                .set(USER_ROLES.USER_ID, userId)
                .set(USER_ROLES.ROLE_ID, field(select(ROLES.ID).from(ROLES).where(ROLES.NAME.eq(it.name))))
                .execute()
        }
    }

    private fun insert(user: User) {
        val id = dsl.insertInto(USERS)
            .set(USERS.EXTERNAL_ID, user.id.value)
            .set(USERS.EMAIL, user.email.value)
            .set(USERS.PASSWORD_HASH, user.passwordHash)
            .set(USERS.FULL_NAME, user.fullName)
            .set(USERS.ACTIVE, user.isActive)
            .returning(USERS.ID)
            .fetchSingle { it.id }
        insertRoles(value<Long?>(id), user.roles)
        user.academicAccounts.forEach {
            dsl.insertInto(ACADEMIC_ACCOUNTS)
                .set(ACADEMIC_ACCOUNTS.USER_ID, id)
                .set(ACADEMIC_ACCOUNTS.INSTITUTION_ID, institutionKey(it.institution))
                .set(ACADEMIC_ACCOUNTS.LOGIN_CIPHERTEXT, it.loginCiphertext)
                .set(ACADEMIC_ACCOUNTS.LOGIN_DIGEST, it.loginDigest)
                .set(ACADEMIC_ACCOUNTS.PASSWORD_CIPHERTEXT, it.passwordCiphertext)
                .execute()
        }
    }
}
