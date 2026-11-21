package br.com.mariofronza.sigabot.entity.enrollment

import br.com.mariofronza.sigabot.entity.catalog.SubjectOffering
import br.com.mariofronza.sigabot.entity.user.UserId

data class Enrollment(
    val studentId: UserId,
    val offering: SubjectOffering,
    val grade: Grade?,
    val absences: Int,
    val attendance: Attendance?,
    val result: String,
) {

    init {
        require(absences >= 0) { "Absences $absences cannot be negative" }
    }

    data class Recorded(val enrollment: Enrollment, val events: List<GradePosted>)

    companion object {
        fun record(studentId: UserId, offering: SubjectOffering, existing: Enrollment?, outcome: SemesterOutcome): Recorded {
            val enrollment = Enrollment(studentId, offering, outcome.grade, outcome.absences, outcome.attendance, outcome.result)
            val grade = outcome.grade
            val events = if (existing != null && grade != null && existing.grade != grade) {
                listOf(GradePosted(studentId, offering, grade))
            } else {
                emptyList()
            }
            return Recorded(enrollment, events)
        }
    }
}
