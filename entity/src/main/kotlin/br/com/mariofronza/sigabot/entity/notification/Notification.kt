package br.com.mariofronza.sigabot.entity.notification

import br.com.mariofronza.sigabot.entity.user.UserId
import java.time.Instant

data class Notification(
    val id: Long? = null,
    val recipient: UserId,
    val message: String,
    val createdAt: Instant? = null,
)
