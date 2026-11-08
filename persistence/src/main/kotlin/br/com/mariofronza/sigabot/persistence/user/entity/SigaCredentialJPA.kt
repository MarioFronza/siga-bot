package br.com.mariofronza.sigabot.persistence.user.entity

import br.com.mariofronza.sigabot.entity.user.SigaCredential
import jakarta.persistence.*

@Entity
@Table(name = "siga_credentials")
data class SigaCredentialJPA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "cpf_hash")
    val cpfHash: String,
    @Column(name = "password_hash")
    val passwordHash: String
) {
    fun toEntity() = SigaCredential(
        id = id,
        cpfHash = cpfHash,
        passwordHash = passwordHash
    )
}
