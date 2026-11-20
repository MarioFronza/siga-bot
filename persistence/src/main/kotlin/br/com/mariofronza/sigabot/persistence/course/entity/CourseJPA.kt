package br.com.mariofronza.sigabot.persistence.course.entity

import br.com.mariofronza.sigabot.entity.course.Course
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

@Entity
@Table(name = "courses")
class CourseJPA(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id")
    val institution: InstitutionJPA,
    @Column(name = "name")
    val name: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun toEntity() = Course(id = id, institution = institution.toEntity(), name = name)
}
