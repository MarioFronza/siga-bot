package br.com.mariofronza.sigabot.persistence.user.entity

import br.com.mariofronza.sigabot.entity.user.Role
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "roles")
class RoleJPA(
    @Id
    val id: Long,
    @Column(name = "name")
    val name: String,
) {
    fun toEntity() = Role(id = id, name = name)
}
