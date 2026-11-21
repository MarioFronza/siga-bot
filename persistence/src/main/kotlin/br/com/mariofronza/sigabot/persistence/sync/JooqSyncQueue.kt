package br.com.mariofronza.sigabot.persistence.sync

import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.SYNC_JOBS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.USERS
import br.com.mariofronza.sigabot.persistence.notNull
import br.com.mariofronza.sigabot.persistence.user.userKey
import br.com.mariofronza.sigabot.usecases.academic.port.SyncJob
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.usecases.academic.port.SyncQueue
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import br.com.mariofronza.sigabot.usecases.academic.port.SyncStatus
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL.`when`
import org.jooq.impl.DSL.castNull
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

    override fun request(userId: UserId, scope: SyncScope, runAt: Instant) {
        dsl.insertInto(SYNC_JOBS)
            .set(SYNC_JOBS.USER_ID, userKey(userId))
            .set(SYNC_JOBS.NEXT_RUN_AT, runAt.utc())
            .set(SYNC_JOBS.REQUESTED_SCOPE, scope.code())
            .onConflict(SYNC_JOBS.USER_ID)
            .doUpdate()
            .set(SYNC_JOBS.NEXT_RUN_AT, excluded(SYNC_JOBS.NEXT_RUN_AT))
            .set(SYNC_JOBS.REQUESTED_SCOPE, excluded(SYNC_JOBS.REQUESTED_SCOPE))
            .execute()
    }

    override fun claimDue(now: Instant, limit: Int, lease: Duration): List<SyncJob> {
        val due = dsl.select(SYNC_JOBS.USER_ID, USERS.EXTERNAL_ID, SYNC_JOBS.ATTEMPTS, SYNC_JOBS.REQUESTED_SCOPE)
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
        return due.map { SyncJob(UserId(it.notNull(USERS.EXTERNAL_ID)), it.notNull(SYNC_JOBS.ATTEMPTS), scopeOf(it.get(SYNC_JOBS.REQUESTED_SCOPE))) }
    }

    override fun completed(userId: UserId, syncedAt: Instant, nextRunAt: Instant, fulfilled: SyncScope?) {
        val requestedMeanwhile = SYNC_JOBS.REQUESTED_SCOPE.isDistinctFrom(fulfilled?.code())
        dsl.update(SYNC_JOBS)
            .set(SYNC_JOBS.LAST_SYNCED_AT, syncedAt.utc())
            .set(SYNC_JOBS.NEXT_RUN_AT, `when`(requestedMeanwhile, SYNC_JOBS.NEXT_RUN_AT).otherwise(nextRunAt.utc()))
            .set(SYNC_JOBS.REQUESTED_SCOPE, `when`(requestedMeanwhile, SYNC_JOBS.REQUESTED_SCOPE).otherwise(castNull(SYNC_JOBS.REQUESTED_SCOPE)))
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
            .setNull(SYNC_JOBS.REQUESTED_SCOPE)
            .where(SYNC_JOBS.USER_ID.eq(userKey(userId)))
            .execute()
    }

    override fun countDue(now: Instant): Long =
        dsl.fetchCount(dsl.selectFrom(SYNC_JOBS.join(USERS).on(USERS.ID.eq(SYNC_JOBS.USER_ID))).where(dueAt(now))).toLong()

    override fun statusOf(userId: UserId, now: Instant): SyncStatus? =
        dsl.selectFrom(SYNC_JOBS)
            .where(SYNC_JOBS.USER_ID.eq(userKey(userId)))
            .fetchOne {
                SyncStatus(
                    lastSyncedAt = it.lastSyncedAt?.toInstant(),
                    nextRunAt = it.notNull(SYNC_JOBS.NEXT_RUN_AT).toInstant(),
                    running = it.lockedUntil?.toInstant()?.isAfter(now) ?: false,
                    pending = it.requestedScope != null,
                    attempts = it.notNull(SYNC_JOBS.ATTEMPTS),
                    lastError = it.lastError,
                )
            }

    private fun dueAt(now: Instant): Condition = SYNC_JOBS.NEXT_RUN_AT.le(now.utc())
        .and(SYNC_JOBS.LOCKED_UNTIL.isNull.or(SYNC_JOBS.LOCKED_UNTIL.lt(now.utc())))
        .and(USERS.ACTIVE.isTrue)

    private fun SyncScope.code(): String? = when (this) {
        SyncScope.Everything -> EVERYTHING
        is SyncScope.Period -> period.toString()
        SyncScope.Latest -> null
    }

    private fun scopeOf(code: String?): SyncScope? = when (code) {
        null -> null
        EVERYTHING -> SyncScope.Everything
        else -> SyncScope.Period(AcademicPeriod.parse(code))
    }

    private fun Instant.utc(): OffsetDateTime = atOffset(ZoneOffset.UTC)

    companion object {
        private const val EVERYTHING = "everything"
    }
}
