package br.com.mariofronza.sigabot.persistence.subject.entity

import br.com.mariofronza.sigabot.entity.subject.Subject
import jakarta.persistence.*

@Entity
@Table(name = "subjects")
data class SubjectJPA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "name")
    val name: String,
    @OneToMany(mappedBy = "subject")
    val enrolledGroups: List<SubjectEnrollmentJPA> = emptyList(),
) {
    fun toEntity() = Subject(
        id = id,
        name = name
    )
}
