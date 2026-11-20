package br.com.mariofronza.sigabot.usecases.user.dto

import br.com.mariofronza.sigabot.entity.user.User
import java.util.UUID

data class UserOutput(
    val id: UUID,
    val email: String,
    val fullName: String,
) {
    companion object {
        fun from(user: User) = UserOutput(id = user.id.value, email = user.email.value, fullName = user.fullName)
    }
}
