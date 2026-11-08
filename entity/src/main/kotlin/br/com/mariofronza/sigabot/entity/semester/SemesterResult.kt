package br.com.mariofronza.sigabot.entity.semester

import br.com.mariofronza.sigabot.entity.subject.SubjectEnrollment
import br.com.mariofronza.sigabot.entity.user.User

data class SemesterResult(
    val id: Long? = null,
    val finalGrade: Double,
    val courseLoad: Int,
    val absencesCount: Int,
    val attendancePercentage: Double,
    val result: String,
    val user: User,
    val subjectEnrolled: SubjectEnrollment
)
