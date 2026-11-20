package br.com.mariofronza.sigabot.persistence.section.entity

import br.com.mariofronza.sigabot.persistence.term.entity.AcademicTermJPA

import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.persistence.course.entity.CourseJPA
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectOfferingJPA
import br.com.mariofronza.sigabot.persistence.user.entity.UserJPA
import jakarta.persistence.*

@Entity
@Table(name = "groups")
data class ClassSectionJPA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "name")
    val name: String,
    @ManyToOne
    @JoinColumn(name = "course_id")
    val course: CourseJPA,
    @ManyToOne
    @JoinColumn(name = "period_id")
    val period: AcademicTermJPA,
    @OneToMany(mappedBy = "enrolledGroup")
    val enrolledSubjects: List<SubjectOfferingJPA> = emptyList(),
    @ManyToMany(mappedBy = "groups")
    val users: List<UserJPA> = emptyList()
) {
    fun toEntity() = ClassSection(
        id = id,
        name = name,
        course = course.toEntity(),
        period = period.toEntity(),
    )
}
