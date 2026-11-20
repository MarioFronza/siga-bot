package br.com.mariofronza.sigabot.persistence.sync

import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.SYNC_JOBS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.USERS
import br.com.mariofronza.sigabot.persistence.notNull
import br.com.mariofronza.sigabot.persistence.user.userKey
import br.com.mariofronza.sigabot.usecases.academic.port.SyncJob
import br.com.mariofronza.sigabot.usecases.academic.port.SyncQueue
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL.excluded
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
@Transactional
class JooqSyncQueue(
    private val dsl: DSLContext,
) : SyncQueue {

    override fun enqueue(userId: UserId, runAt: Instant) {
        dsl.insertInto(SYNC_JOBS)
            .set(SYNC_JOBS.USER_ID, userKey(userId))
            .set(SYNC_JOBS.NEXT_RUN_AT, runAt.utc())
            .onConflict(SYNC_JOBS.USER_ID)
            .doUpdate().set(SYNC_JOBS.NEXT_RUN_AT, excluded(SYNC_JOBS.NEXT_RUN_AT))
            .execute()
    }

    override fun claimDue(now: Instant, limit: Int, lease: Duration): List<SyncJob> {
        val due = dsl.select(SYNC_JOBS.USER_ID, USERS.EXTERNAL_ID, SYNC_JOBS.ATTEMPTS)
            .from(SYNC_JOBS)
            .join(USERS).on(USERS.ID.eq(SYNC_JOBS.USER_ID))
            .where(dueAt(now))
            .orderBy(SYNC_JOBS.NEXT_RUN_AT)
            .limit(limit)
            .forUpdate().of(SYNC_JOBS).skipLocked()
            .fetch()
        if (due.isEmpty()) return emptyList()

        dsl.update(SYNC_JOBS)
            .set(SYNC_JOBS.LOCKED_UNTIL, now.plus(lease).utc())
            .where(SYNC_JOBS.USER_ID.`in`(due.map { it.notNull(SYNC_JOBS.USER_ID) }))
            .execute()
        return due.map { SyncJob(UserId(it.notNull(USERS.EXTERNAL_ID)), it.notNull(SYNC_JOBS.ATTEMPTS)) }
    }

    override fun completed(userId: UserId, nextRunAt: Instant) {
        dsl.update(SYNC_JOBS)
            .set(SYNC_JOBS.NEXT_RUN_AT, nextRunAt.utc())
            .setNull(SYNC_JOBS.LOCKED_UNTIL)
            .set(SYNC_JOBS.ATTEMPTS, 0)
            .setNull(SYNC_JOBS.LAST_ERROR)
            .where(SYNC_JOBS.USER_ID.eq(userKey(userId)))
            .execute()
    }

    override fun failed(userId: UserId, error: String, retryAt: Instant) {
        dsl.update(SYNC_JOBS)
            .set(SYNC_JOBS.NEXT_RUN_AT, retryAt.utc())
            .setNull(SYNC_JOBS.LOCKED_UNTIL)
            .set(SYNC_JOBS.ATTEMPTS, SYNC_JOBS.ATTEMPTS.plus(1))
            .set(SYNC_JOBS.LAST_ERROR, error)
            .where(SYNC_JOBS.USER_ID.eq(userKey(userId)))
            .execute()
    }

    override fun countDue(now: Instant): Long =
        dsl.fetchCount(dsl.selectFrom(SYNC_JOBS.join(USERS).on(USERS.ID.eq(SYNC_JOBS.USER_ID))).where(dueAt(now))).toLong()

    private fun dueAt(now: Instant): Condition = SYNC_JOBS.NEXT_RUN_AT.le(now.utc())
        .and(SYNC_JOBS.LOCKED_UNTIL.isNull.or(SYNC_JOBS.LOCKED_UNTIL.lt(now.utc())))
        .and(USERS.ACTIVE.isTrue)

    private fun Instant.utc(): OffsetDateTime = atOffset(ZoneOffset.UTC)
}
