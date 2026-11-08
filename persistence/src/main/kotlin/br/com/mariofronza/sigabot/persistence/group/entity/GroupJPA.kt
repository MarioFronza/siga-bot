package br.com.mariofronza.sigabot.persistence.group.entity

import br.com.mariofronza.sigabot.entity.group.Group
import br.com.mariofronza.sigabot.persistence.course.entity.CourseJPA
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectEnrollmentJPA
import br.com.mariofronza.sigabot.persistence.user.entity.UserJPA
import jakarta.persistence.*

@Entity
@Table(name = "groups")
data class GroupJPA(
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
    val period: PeriodJPA,
    @OneToMany(mappedBy = "enrolledGroup")
    val enrolledSubjects: List<SubjectEnrollmentJPA> = emptyList(),
    @ManyToMany(mappedBy = "groups")
    val users: List<UserJPA> = emptyList()
) {
    fun toEntity() = Group(
        id = id,
        name = name,
        course = course.toEntity(),
        period = period.toEntity(),
    )
}
