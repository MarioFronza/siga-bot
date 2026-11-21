package br.com.mariofronza.sigabot.persistence.notification

import br.com.mariofronza.sigabot.persistence.user.userKey
import br.com.mariofronza.sigabot.persistence.notNull
import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.notification.NotificationKind
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.USERS
import org.jooq.impl.DSL.inline
import org.jooq.impl.DSL.select
import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.NOTIFICATIONS
import br.com.mariofronza.sigabot.usecases.notification.port.NotificationRepository
import org.jooq.DSLContext
import br.com.mariofronza.sigabot.usecases.shared.Page
import br.com.mariofronza.sigabot.usecases.shared.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class JooqNotificationRepository(
    private val dsl: DSLContext,
) : NotificationRepository {

    override fun findByRecipient(recipient: UserId, request: PageRequest): Page<Notification> {
        val ofUser = NOTIFICATIONS.USER_ID.eq(userKey(recipient))
        val notifications = dsl.selectFrom(NOTIFICATIONS)
            .where(ofUser)
            .orderBy(NOTIFICATIONS.CREATED_AT.desc(), NOTIFICATIONS.ID.desc())
            .limit(request.size)
            .offset(request.offset)
            .fetch {
                Notification(
                    id = it.id,
                    recipient = recipient,
                    message = it.message,
                    kind = NotificationKind.valueOf(it.kind),
                    createdAt = it.notNull(NOTIFICATIONS.CREATED_AT).toInstant(),
                )
            }
        return Page(notifications, request, dsl.fetchCount(NOTIFICATIONS, ofUser).toLong())
    }

    override fun save(notification: Notification): Notification {
        val saved = dsl.insertInto(NOTIFICATIONS)
            .set(NOTIFICATIONS.USER_ID, userKey(notification.recipient))
            .set(NOTIFICATIONS.MESSAGE, notification.message)
            .set(NOTIFICATIONS.KIND, notification.kind.name)
            .returning(NOTIFICATIONS.ID, NOTIFICATIONS.CREATED_AT)
            .fetchSingle()
        return notification.copy(id = saved.id, createdAt = saved.notNull(NOTIFICATIONS.CREATED_AT).toInstant())
    }

    override fun saveForEveryActiveUser(message: String, kind: NotificationKind) {
        dsl.insertInto(NOTIFICATIONS, NOTIFICATIONS.USER_ID, NOTIFICATIONS.MESSAGE, NOTIFICATIONS.KIND)
            .select(select(USERS.ID, inline(message), inline(kind.name)).from(USERS).where(USERS.ACTIVE.isTrue))
            .execute()
    }
}
