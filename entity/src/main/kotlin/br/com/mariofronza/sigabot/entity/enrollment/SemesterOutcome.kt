package br.com.mariofronza.sigabot.entity.enrollment

data class SemesterOutcome(
    val grade: Grade,
    val absences: Int,
    val attendance: Attendance,
    val result: String,
)
