package br.com.mariofronza.sigabot.persistence.notification

import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.OUTBOX
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.USERS
import br.com.mariofronza.sigabot.persistence.notNull
import br.com.mariofronza.sigabot.persistence.user.userKey
import br.com.mariofronza.sigabot.usecases.notification.port.Outbox
import br.com.mariofronza.sigabot.usecases.notification.port.OutboxMessage
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL.currentOffsetDateTime
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
@Transactional
class JooqOutbox(
    private val dsl: DSLContext,
) : Outbox {

    override fun add(recipient: UserId, message: String) {
        dsl.insertInto(OUTBOX)
            .set(OUTBOX.USER_ID, userKey(recipient))
            .set(OUTBOX.MESSAGE, message)
            .execute()
    }

    override fun claimPending(now: Instant, limit: Int, lease: Duration, maxAttempts: Int): List<OutboxMessage> {
        val claimed = dsl.select(OUTBOX.ID, USERS.EXTERNAL_ID, OUTBOX.MESSAGE, OUTBOX.ATTEMPTS)
            .from(OUTBOX)
            .join(USERS).on(USERS.ID.eq(OUTBOX.USER_ID))
            .where(
                pendingBelow(maxAttempts),
                OUTBOX.NEXT_ATTEMPT_AT.le(now.utc()),
                OUTBOX.LOCKED_UNTIL.isNull.or(OUTBOX.LOCKED_UNTIL.lt(now.utc())),
            )
            .orderBy(OUTBOX.ID)
            .limit(limit)
            .forUpdate().of(OUTBOX).skipLocked()
            .fetch {
                OutboxMessage(
                    id = it.notNull(OUTBOX.ID),
                    recipient = UserId(it.notNull(USERS.EXTERNAL_ID)),
                    message = it.notNull(OUTBOX.MESSAGE),
                    attempts = it.notNull(OUTBOX.ATTEMPTS),
                )
            }
        if (claimed.isNotEmpty()) {
            dsl.update(OUTBOX)
                .set(OUTBOX.LOCKED_UNTIL, now.plus(lease).utc())
                .where(OUTBOX.ID.`in`(claimed.map(OutboxMessage::id)))
                .execute()
        }
        return claimed
    }

    override fun delivered(id: Long) {
        dsl.update(OUTBOX)
            .set(OUTBOX.DELIVERED_AT, currentOffsetDateTime())
            .setNull(OUTBOX.LOCKED_UNTIL)
            .where(OUTBOX.ID.eq(id))
            .execute()
    }

    override fun failed(id: Long, retryAt: Instant) {
        dsl.update(OUTBOX)
            .set(OUTBOX.ATTEMPTS, OUTBOX.ATTEMPTS.plus(1))
            .set(OUTBOX.NEXT_ATTEMPT_AT, retryAt.utc())
            .setNull(OUTBOX.LOCKED_UNTIL)
            .where(OUTBOX.ID.eq(id))
            .execute()
    }

    override fun countPending(maxAttempts: Int): Long = dsl.fetchCount(OUTBOX, pendingBelow(maxAttempts)).toLong()

    private fun pendingBelow(maxAttempts: Int): Condition = OUTBOX.DELIVERED_AT.isNull.and(OUTBOX.ATTEMPTS.lt(maxAttempts))

    private fun Instant.utc(): OffsetDateTime = atOffset(ZoneOffset.UTC)
}
