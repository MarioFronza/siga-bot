package br.com.mariofronza.sigabot.persistence.user.entity

import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.group.entity.GroupJPA
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class UserJPA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "is_active")
    var isActive: Boolean,
    @Column(name = "full_name")
    val fullName: String,
    @Column(name = "username")
    val username: String,
    @Column(name = "email")
    val email: String,
    @Column(name = "password_hash")
    var passwordHash: String,
    @ManyToMany(fetch = FetchType.EAGER)
    val roles: MutableList<RoleJPA>,
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val sigaCredential: SigaCredentialJPA,
    @ManyToMany(fetch = FetchType.EAGER)
    val groups: Set<GroupJPA>
) {
    fun toEntity() = User(
        id = id,
        isActive = isActive,
        fullName = fullName,
        email = email,
        passwordHash = passwordHash,
        roles = roles.map { it.toEntity() },
        sigaCredential = sigaCredential.toEntity(),
        groups = groups.map { it.toEntity() }.toMutableList()
    )
}
