package br.com.mariofronza.sigabot.persistence.user.entity

import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.persistence.institution.entity.InstitutionJPA
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(name = "academic_accounts")
class AcademicAccountJPA(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    val user: UserJPA,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id")
    val institution: InstitutionJPA,
    @Column(name = "login_ciphertext")
    var loginCiphertext: String,
    @Column(name = "login_digest")
    var loginDigest: ByteArray,
    @Column(name = "password_ciphertext")
    var passwordCiphertext: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: Instant? = null

    fun toEntity() = AcademicAccount(
        institution = institution.toEntity(),
        loginCiphertext = loginCiphertext,
        loginDigest = loginDigest,
        passwordCiphertext = passwordCiphertext,
    )
}
