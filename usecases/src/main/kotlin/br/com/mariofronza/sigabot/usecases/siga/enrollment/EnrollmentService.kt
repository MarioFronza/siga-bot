package br.com.mariofronza.sigabot.usecases.siga.enrollment

import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.enrollment.Grade
import br.com.mariofronza.sigabot.entity.enrollment.Attendance
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SemesterResultOutput
import br.com.mariofronza.sigabot.usecases.siga.enrollment.gateway.EnrollmentInteractor
import org.springframework.stereotype.Service

@Service
class EnrollmentService(
    private val enrollmentInteractor: EnrollmentInteractor,
) {

    fun findAll(user: User, period: AcademicPeriod): List<Enrollment> =
        enrollmentInteractor.findAll(user, period)

    fun hasGradeChanged(user: User, subjectOffering: SubjectOffering, result: SemesterResultOutput): Boolean {
        val enrollment = enrollmentInteractor.find(user, subjectOffering) ?: return false
        return Grade(result.finalGrade) != enrollment.finalGrade
    }

    fun record(user: User, subjectOffering: SubjectOffering, result: SemesterResultOutput): Enrollment {
        return enrollmentInteractor.save(
            Enrollment(
                id = enrollmentInteractor.find(user, subjectOffering)?.id,
                user = user,
                subjectOffering = subjectOffering,
                finalGrade = Grade(result.finalGrade),
                absences = result.absencesCount,
                attendance = Attendance(result.attendancePercentage),
                result = result.result,
            )
        )
    }
}
