package br.com.mariofronza.sigabot.usecases.siga.enrollment.dto

import br.com.mariofronza.sigabot.entity.enrollment.Enrollment

data class EnrollmentOutput(
    val subject: String,
    val classSection: String,
    val courseLoad: Int,
    val finalGrade: Double,
    val absences: Int,
    val attendancePercentage: Double,
    val result: String,
) {
    companion object {
        fun from(enrollment: Enrollment) = EnrollmentOutput(
            subject = enrollment.offering.subject,
            classSection = enrollment.offering.classSection,
            courseLoad = enrollment.offering.courseLoad,
            finalGrade = enrollment.grade.value,
            absences = enrollment.absences,
            attendancePercentage = enrollment.attendance.percentage,
            result = enrollment.result,
        )
    }
}
