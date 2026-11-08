package br.com.mariofronza.sigabot.persistence.notification.repository

import br.com.mariofronza.sigabot.persistence.notification.entity.NotificationJPA
import br.com.mariofronza.sigabot.persistence.user.entity.UserJPA
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<NotificationJPA, Long> {

    fun findByUser(user: UserJPA): List<NotificationJPA>
}
