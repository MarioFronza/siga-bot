package br.com.mariofronza.sigabot.persistence.notification.entity

import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.persistence.user.entity.UserJPA
import jakarta.persistence.*

@Entity
@Table(name = "notifications")
data class NotificationJPA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "entity_id")
    val entityId: Long,
    @Column(name = "is_active")
    val description: String,
    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: UserJPA,
) {

    fun toEntity() = Notification(
        id = id,
        entityId = entityId,
        description = description,
        user = user.toEntity()
    )
}
