package br.com.mariofronza.sigabot.entity.notification

import br.com.mariofronza.sigabot.entity.user.User

data class Notification(
    val id: Long? = null,
    val description: String,
    val entityId: Long,
    val user: User,
)
