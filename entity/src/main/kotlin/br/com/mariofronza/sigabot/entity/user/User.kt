package br.com.mariofronza.sigabot.entity.user

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode

data class User(
    val id: UserId = UserId.generate(),
    val isActive: Boolean,
    val fullName: String = "",
    val email: Email,
    val passwordHash: String,
    val roles: Set<Role>,
    val academicAccounts: List<AcademicAccount>,
) {
    val username: String
        get() = if (fullName.isNotEmpty()) fullName.split(" ").first() else ""

    fun accountAt(institution: InstitutionCode): AcademicAccount =
        academicAccounts.firstOrNull { it.institution == institution } ?: throw NoAcademicAccountException(id, institution)

    fun rename(fullName: String) = copy(fullName = fullName)

    companion object {
        fun registerStudent(email: Email, passwordHash: String, account: AcademicAccount) = User(
            isActive = true,
            email = email,
            passwordHash = passwordHash,
            roles = setOf(Role.STUDENT),
            academicAccounts = listOf(account),
        )
    }
}
