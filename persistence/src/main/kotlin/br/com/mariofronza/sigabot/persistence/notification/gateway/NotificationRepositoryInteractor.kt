package br.com.mariofronza.sigabot.persistence.notification.gateway

import br.com.mariofronza.sigabot.persistence.notNull
import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.NOTIFICATIONS
import br.com.mariofronza.sigabot.usecases.notification.gateway.NotificationInteractor
import org.jooq.DSLContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class NotificationRepositoryInteractor(
    private val dsl: DSLContext,
) : NotificationInteractor {

    override fun findByUser(user: User, pageable: Pageable): Page<Notification> {
        val ofUser = NOTIFICATIONS.USER_ID.eq(user.id)
        val notifications = dsl.selectFrom(NOTIFICATIONS)
            .where(ofUser)
            .orderBy(NOTIFICATIONS.CREATED_AT.desc(), NOTIFICATIONS.ID.desc())
            .limit(pageable.pageSize)
            .offset(pageable.offset)
            .fetch { Notification(id = it.id, user = user, message = it.message, createdAt = it.notNull(NOTIFICATIONS.CREATED_AT).toInstant()) }
        return PageImpl(notifications, PageRequest.of(pageable.pageNumber, pageable.pageSize), dsl.fetchCount(NOTIFICATIONS, ofUser).toLong())
    }

    override fun save(notification: Notification): Notification {
        val saved = dsl.insertInto(NOTIFICATIONS)
            .set(NOTIFICATIONS.USER_ID, notification.user.id)
            .set(NOTIFICATIONS.MESSAGE, notification.message)
            .returning(NOTIFICATIONS.ID, NOTIFICATIONS.CREATED_AT)
            .fetchSingle()
        return notification.copy(id = saved.id, createdAt = saved.notNull(NOTIFICATIONS.CREATED_AT).toInstant())
    }
}
