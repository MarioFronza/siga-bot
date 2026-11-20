package br.com.mariofronza.sigabot.persistence.user.entity

import br.com.mariofronza.sigabot.entity.user.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class UserJPA(
    @Column(name = "email")
    var email: String,
    @Column(name = "password_hash")
    var passwordHash: String,
    @Column(name = "full_name")
    var fullName: String,
    @Column(name = "active")
    var active: Boolean,
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: MutableSet<RoleJPA>,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "external_id", updatable = false)
    val externalId: UUID? = null

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], optional = false)
    lateinit var sigaCredential: SigaCredentialJPA

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: Instant? = null

    fun toEntity() = User(
        id = id,
        externalId = externalId,
        isActive = active,
        fullName = fullName,
        email = email,
        passwordHash = passwordHash,
        roles = roles.map { it.toEntity() },
        sigaCredential = sigaCredential.toEntity(),
    )
}
