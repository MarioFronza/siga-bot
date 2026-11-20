package br.com.mariofronza.sigabot.persistence.notification.repository

import br.com.mariofronza.sigabot.persistence.notification.entity.NotificationJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<NotificationJPA, Long> {

    fun findByUserIdOrderByCreatedAtDescIdDesc(userId: Long, pageable: Pageable): Page<NotificationJPA>
}
