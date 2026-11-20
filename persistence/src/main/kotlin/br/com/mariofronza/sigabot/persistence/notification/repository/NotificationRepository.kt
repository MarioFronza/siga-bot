package br.com.mariofronza.sigabot.persistence.notification.repository

import br.com.mariofronza.sigabot.persistence.notification.entity.NotificationJPA
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<NotificationJPA, Long> {

    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<NotificationJPA>
}
