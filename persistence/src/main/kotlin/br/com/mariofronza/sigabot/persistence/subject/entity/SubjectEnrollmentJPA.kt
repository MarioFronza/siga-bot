package br.com.mariofronza.sigabot.persistence.subject.entity

import br.com.mariofronza.sigabot.entity.subject.SubjectEnrollment
import br.com.mariofronza.sigabot.persistence.group.entity.GroupJPA
import br.com.mariofronza.sigabot.persistence.semester.entity.SemesterResultJPA
import jakarta.persistence.*

@Entity
@Table(name = "subjects_enrollment")
data class SubjectEnrollmentJPA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "group_id")
    val enrolledGroup: GroupJPA,
    @ManyToOne
    @JoinColumn(name = "subject_id")
    val subject: SubjectJPA,
    @OneToMany(mappedBy = "subjectEnrolled")
    val semesterResults: List<SemesterResultJPA> = emptyList(),
) {

    fun toEntity() = SubjectEnrollment(
        id = id,
        subject = subject.toEntity(),
        enrolledGroup = enrolledGroup.toEntity()
    )
}
