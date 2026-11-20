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
            subject = enrollment.subjectOffering.subject.name,
            classSection = enrollment.subjectOffering.classSection.code,
            courseLoad = enrollment.subjectOffering.courseLoad,
            finalGrade = enrollment.finalGrade,
            absences = enrollment.absences,
            attendancePercentage = enrollment.attendancePercentage,
            result = enrollment.result,
        )
    }
}
