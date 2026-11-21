package br.com.mariofronza.sigabot.http.controller

import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.user.User
import java.time.Instant
import java.util.UUID

data class UserResponse(val id: UUID, val email: String, val fullName: String) {
    companion object {
        fun from(user: User) = UserResponse(id = user.id.value, email = user.email.value, fullName = user.fullName)
    }
}

data class PeriodResponse(val year: Int, val term: Int) {
    companion object {
        fun from(period: AcademicPeriod) = PeriodResponse(year = period.year, term = period.term)
    }
}

data class EnrollmentResponse(
    val subject: String,
    val classSection: String,
    val courseLoad: Int,
    val finalGrade: Double?,
    val absences: Int,
    val attendancePercentage: Double?,
    val result: String,
) {
    companion object {
        fun from(enrollment: Enrollment) = EnrollmentResponse(
            subject = enrollment.offering.subject,
            classSection = enrollment.offering.classSection,
            courseLoad = enrollment.offering.courseLoad,
            finalGrade = enrollment.grade?.value,
            absences = enrollment.absences,
            attendancePercentage = enrollment.attendance?.percentage,
            result = enrollment.result,
        )
    }
}

data class NotificationResponse(val id: Long, val message: String, val createdAt: Instant) {
    companion object {
        fun from(notification: Notification) = NotificationResponse(
            id = checkNotNull(notification.id) { "Notification has no id until it is saved" },
            message = notification.message,
            createdAt = checkNotNull(notification.createdAt) { "Notification has no creation time until it is saved" },
        )
    }
}
