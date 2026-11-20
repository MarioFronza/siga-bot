package br.com.mariofronza.sigabot.entity.user

data class User(
    val id: UserId = UserId.generate(),
    val isActive: Boolean,
    val fullName: String = "",
    val email: Email,
    val passwordHash: String,
    val roles: List<Role>,
    val academicAccounts: List<AcademicAccount>,
) {
    val username: String
        get() = if (fullName.isNotEmpty()) fullName.split(" ").first() else ""
}
