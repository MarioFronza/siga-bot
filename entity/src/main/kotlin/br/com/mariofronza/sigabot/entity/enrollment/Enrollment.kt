package br.com.mariofronza.sigabot.entity.enrollment

import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.entity.user.User

data class Enrollment(
    val id: Long? = null,
    val user: User,
    val subjectOffering: SubjectOffering,
    val finalGrade: Double,
    val absences: Int,
    val attendancePercentage: Double,
    val result: String,
)
