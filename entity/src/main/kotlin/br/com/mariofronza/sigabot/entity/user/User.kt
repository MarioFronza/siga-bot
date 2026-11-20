package br.com.mariofronza.sigabot.entity.user

import br.com.mariofronza.sigabot.entity.section.ClassSection

data class User(
    val id: Long? = null,
    val isActive: Boolean,
    val fullName: String = "",
    val email: String,
    val passwordHash: String,
    val roles: List<Role>,
    val sigaCredential: SigaCredential,
    val groups: MutableList<ClassSection> = emptyList<ClassSection>().toMutableList()
) {
    val username: String
        get() = if (fullName.isNotEmpty()) fullName.split(" ").first() else ""
}
