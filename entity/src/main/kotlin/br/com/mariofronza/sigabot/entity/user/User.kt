package br.com.mariofronza.sigabot.entity.user

import java.util.UUID

data class User(
    val id: Long? = null,
    val externalId: UUID? = null,
    val isActive: Boolean,
    val fullName: String = "",
    val email: String,
    val passwordHash: String,
    val roles: List<Role>,
    val sigaCredential: SigaCredential,
) {
    val username: String
        get() = if (fullName.isNotEmpty()) fullName.split(" ").first() else ""
}
