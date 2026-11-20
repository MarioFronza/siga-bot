package br.com.mariofronza.sigabot.persistence.section.entity

import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.persistence.course.entity.CourseJPA
import br.com.mariofronza.sigabot.persistence.term.entity.AcademicTermJPA
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
@Table(name = "class_sections")
class ClassSectionJPA(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_term_id")
    val academicTerm: AcademicTermJPA,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    val course: CourseJPA,
    @Column(name = "code")
    val code: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun toEntity() = ClassSection(
        id = id,
        code = code,
        course = course.toEntity(),
        academicTerm = academicTerm.toEntity(),
    )
}
