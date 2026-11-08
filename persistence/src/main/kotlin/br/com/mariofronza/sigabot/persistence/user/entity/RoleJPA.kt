package br.com.mariofronza.sigabot.persistence.user.entity

import br.com.mariofronza.sigabot.entity.user.Role
import jakarta.persistence.*

@Entity
@Table(name = "roles")
data class RoleJPA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "name")
    val name: String
) {
    fun toEntity() = Role(
        id = id,
        name = name
    )
}
