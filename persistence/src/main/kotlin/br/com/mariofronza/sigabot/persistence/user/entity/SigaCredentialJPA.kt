package br.com.mariofronza.sigabot.persistence.user.entity

import br.com.mariofronza.sigabot.entity.user.SigaCredential
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(name = "siga_credentials")
class SigaCredentialJPA(
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserJPA,
    @Column(name = "cpf_ciphertext")
    var cpfCiphertext: String,
    @Column(name = "cpf_digest")
    var cpfDigest: ByteArray,
    @Column(name = "password_ciphertext")
    var passwordCiphertext: String,
) {
    @Id
    @Column(name = "user_id")
    val userId: Long? = null

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: Instant? = null

    fun toEntity() = SigaCredential(
        cpfCiphertext = cpfCiphertext,
        cpfDigest = cpfDigest,
        passwordCiphertext = passwordCiphertext,
    )
}
