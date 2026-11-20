package br.com.mariofronza.sigabot.entity.notification

import br.com.mariofronza.sigabot.entity.user.User

data class Notification(
    val id: Long? = null,
    val user: User,
    val message: String,
)
