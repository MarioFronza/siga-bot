package br.com.mariofronza.sigabot.entity.enrollment

import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.entity.user.User

data class Enrollment(
    val id: Long? = null,
    val finalGrade: Double,
    val courseLoad: Int,
    val absencesCount: Int,
    val attendancePercentage: Double,
    val result: String,
    val user: User,
    val subjectEnrolled: SubjectOffering
)
