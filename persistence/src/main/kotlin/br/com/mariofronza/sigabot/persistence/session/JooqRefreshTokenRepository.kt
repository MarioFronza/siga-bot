package br.com.mariofronza.sigabot.persistence.session

import br.com.mariofronza.sigabot.entity.session.RefreshToken
import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.REFRESH_TOKENS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.USERS
import br.com.mariofronza.sigabot.persistence.user.userKey
import br.com.mariofronza.sigabot.usecases.identity.port.RefreshTokenRepository
import org.jooq.DSLContext
import org.jooq.impl.DSL.excluded
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Component
@Transactional
class JooqRefreshTokenRepository(
    private val dsl: DSLContext,
) : RefreshTokenRepository {

    override fun save(token: RefreshToken) {
        dsl.insertInto(REFRESH_TOKENS)
            .set(REFRESH_TOKENS.ID, token.id)
            .set(REFRESH_TOKENS.USER_ID, userKey(token.userId))
            .set(REFRESH_TOKENS.FAMILY_ID, token.familyId)
            .set(REFRESH_TOKENS.TOKEN_HASH, token.tokenHash)
            .set(REFRESH_TOKENS.EXPIRES_AT, token.expiresAt.utc())
            .set(REFRESH_TOKENS.REVOKED_AT, token.revokedAt?.utc())
            .onConflict(REFRESH_TOKENS.ID)
            .doUpdate().set(REFRESH_TOKENS.REVOKED_AT, excluded(REFRESH_TOKENS.REVOKED_AT))
            .execute()
    }

    override fun findByHash(tokenHash: ByteArray): RefreshToken? =
        dsl.select(REFRESH_TOKENS, USERS.EXTERNAL_ID)
            .from(REFRESH_TOKENS)
            .join(USERS).on(USERS.ID.eq(REFRESH_TOKENS.USER_ID))
            .where(REFRESH_TOKENS.TOKEN_HASH.eq(tokenHash))
            .fetchOne { (token, externalId) ->
                RefreshToken(
                    id = token.id,
                    userId = UserId(checkNotNull(externalId) { "users.external_id is NOT NULL in the schema" }),
                    familyId = token.familyId,
                    tokenHash = token.tokenHash,
                    expiresAt = token.expiresAt.toInstant(),
                    revokedAt = token.revokedAt?.toInstant(),
                )
            }

    override fun revokeFamily(familyId: UUID, now: Instant) {
        dsl.update(REFRESH_TOKENS)
            .set(REFRESH_TOKENS.REVOKED_AT, now.utc())
            .where(REFRESH_TOKENS.FAMILY_ID.eq(familyId).and(REFRESH_TOKENS.REVOKED_AT.isNull))
            .execute()
    }

    private fun Instant.utc(): OffsetDateTime = atOffset(ZoneOffset.UTC)
}
