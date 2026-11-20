package br.com.mariofronza.sigabot.persistence.subject.entity

import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.persistence.section.entity.ClassSectionJPA
import br.com.mariofronza.sigabot.persistence.enrollment.entity.EnrollmentJPA
import jakarta.persistence.*

@Entity
@Table(name = "subjects_enrollment")
data class SubjectOfferingJPA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "group_id")
    val enrolledGroup: ClassSectionJPA,
    @ManyToOne
    @JoinColumn(name = "subject_id")
    val subject: SubjectJPA,
    @OneToMany(mappedBy = "subjectEnrolled")
    val semesterResults: List<EnrollmentJPA> = emptyList(),
) {

    fun toEntity() = SubjectOffering(
        id = id,
        subject = subject.toEntity(),
        enrolledGroup = enrolledGroup.toEntity()
    )
}
