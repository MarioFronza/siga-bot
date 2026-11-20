package br.com.mariofronza.sigabot.persistence.subject.entity

import br.com.mariofronza.sigabot.entity.subject.Subject
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "subjects")
class SubjectJPA(
    @Column(name = "name")
    val name: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun toEntity() = Subject(id = id, name = name)
}
