package br.com.mariofronza.sigabot.usecases.siga.enrollment

import br.com.mariofronza.sigabot.entity.term.AcademicTerm
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

    fun listAllByUserAndPeriod(user: User, period: AcademicTerm): List<Enrollment> {
        return enrollmentInteractor.findAllByUserAndPeriod(user, period)
    }

    fun createSemesterResult(
        subjectEnrollment: SubjectOffering,
        user: User,
        semesterResultOutput: SemesterResultOutput
    ): Enrollment {
        val semesterResult =
            enrollmentInteractor.findBySubjectEnrollmentAndUser(subjectEnrollment, user)

        return enrollmentInteractor.save(
            Enrollment(
                id = semesterResult?.id,
                finalGrade = semesterResultOutput.finalGrade,
                courseLoad = semesterResultOutput.courseLoad,
                absencesCount = semesterResultOutput.absencesCount,
                attendancePercentage = semesterResultOutput.attendancePercentage,
                result = semesterResultOutput.result,
                user = user,
                subjectEnrolled = subjectEnrollment
            )
        )
    }

    fun isThereAnyGradeUpdate(
        subjectEnrollment: SubjectOffering,
        user: User,
        newSemesterResult: SemesterResultOutput
    ): Boolean {
        val semesterResult =
            enrollmentInteractor.findBySubjectEnrollmentAndUser(subjectEnrollment, user)

        if (semesterResult != null) {
            return newSemesterResult.finalGrade != semesterResult.finalGrade
        }

        return false
    }
}
