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
    val academicAccounts: List<AcademicAccount>,
) {
    val username: String
        get() = if (fullName.isNotEmpty()) fullName.split(" ").first() else ""

    fun requireExternalId(): UUID = checkNotNull(externalId) { "User $email has no external id until it is saved" }
}
