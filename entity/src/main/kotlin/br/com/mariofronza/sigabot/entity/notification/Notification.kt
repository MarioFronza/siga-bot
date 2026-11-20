package br.com.mariofronza.sigabot.entity.notification

import br.com.mariofronza.sigabot.entity.user.User
import java.time.Instant

data class Notification(
    val id: Long? = null,
    val user: User,
    val message: String,
    val createdAt: Instant? = null,
)
