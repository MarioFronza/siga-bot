package br.com.mariofronza.sigabot.usecases.siga.enrollment

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

    fun findAll(user: User, year: Int, term: Int): List<Enrollment> =
        enrollmentInteractor.findAll(user, year, term)

    fun hasGradeChanged(user: User, subjectOffering: SubjectOffering, result: SemesterResultOutput): Boolean {
        val enrollment = enrollmentInteractor.find(user, subjectOffering) ?: return false
        return result.finalGrade != enrollment.finalGrade
    }

    fun record(user: User, subjectOffering: SubjectOffering, result: SemesterResultOutput): Enrollment {
        return enrollmentInteractor.save(
            Enrollment(
                id = enrollmentInteractor.find(user, subjectOffering)?.id,
                user = user,
                subjectOffering = subjectOffering,
                finalGrade = result.finalGrade,
                absences = result.absencesCount,
                attendancePercentage = result.attendancePercentage,
                result = result.result,
            )
        )
    }
}
