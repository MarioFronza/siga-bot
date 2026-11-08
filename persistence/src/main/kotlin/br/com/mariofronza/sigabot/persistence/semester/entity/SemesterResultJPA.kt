package br.com.mariofronza.sigabot.persistence.semester.entity

import br.com.mariofronza.sigabot.entity.semester.SemesterResult
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectEnrollmentJPA
import br.com.mariofronza.sigabot.persistence.user.entity.UserJPA
import jakarta.persistence.*

@Entity
@Table(name = "semesters_result")
data class SemesterResultJPA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "final_grade")
    val finalGrade: Double,
    @Column(name = "course_load")
    val courseLoad: Int,
    @Column(name = "absences_count")
    val absencesCount: Int,
    @Column(name = "attendance_percentage")
    val attendancePercentage: Double,
    @Column(name = "result")
    val result: String,
    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: UserJPA,
    @ManyToOne
    @JoinColumn(name = "subject_enrollment_id")
    val subjectEnrolled: SubjectEnrollmentJPA,
) {
    fun toEntity() = SemesterResult(
        id = id,
        finalGrade = finalGrade,
        courseLoad = courseLoad,
        absencesCount = absencesCount,
        attendancePercentage = attendancePercentage,
        result = result,
        user = user.toEntity(),
        subjectEnrolled = subjectEnrolled.toEntity()
    )
}
