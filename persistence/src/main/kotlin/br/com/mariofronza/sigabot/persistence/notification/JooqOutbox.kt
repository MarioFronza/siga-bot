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

    override fun pending(limit: Int, maxAttempts: Int): List<OutboxMessage> =
        dsl.select(OUTBOX.ID, USERS.EXTERNAL_ID, OUTBOX.MESSAGE)
            .from(OUTBOX)
            .join(USERS).on(USERS.ID.eq(OUTBOX.USER_ID))
            .where(pendingBelow(maxAttempts))
            .orderBy(OUTBOX.ID)
            .limit(limit)
            .fetch { OutboxMessage(it.notNull(OUTBOX.ID), UserId(it.notNull(USERS.EXTERNAL_ID)), it.notNull(OUTBOX.MESSAGE)) }

    override fun delivered(id: Long) {
        dsl.update(OUTBOX).set(OUTBOX.DELIVERED_AT, currentOffsetDateTime()).where(OUTBOX.ID.eq(id)).execute()
    }

    override fun failed(id: Long) {
        dsl.update(OUTBOX).set(OUTBOX.ATTEMPTS, OUTBOX.ATTEMPTS.plus(1)).where(OUTBOX.ID.eq(id)).execute()
    }

    override fun countPending(maxAttempts: Int): Long = dsl.fetchCount(OUTBOX, pendingBelow(maxAttempts)).toLong()

    private fun pendingBelow(maxAttempts: Int): Condition = OUTBOX.DELIVERED_AT.isNull.and(OUTBOX.ATTEMPTS.lt(maxAttempts))
}
