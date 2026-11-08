package br.com.mariofronza.sigabot.usecases.siga.semester

import br.com.mariofronza.sigabot.entity.group.Period
import br.com.mariofronza.sigabot.entity.semester.SemesterResult
import br.com.mariofronza.sigabot.entity.subject.SubjectEnrollment
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SemesterResultOutput
import br.com.mariofronza.sigabot.usecases.siga.semester.gateway.SemesterResultsInteractor
import org.springframework.stereotype.Service

@Service
class SemesterResultsService(
    private val semesterResultsInteractor: SemesterResultsInteractor,
) {

    fun listAllByUserAndPeriod(user: User, period: Period): List<SemesterResult> {
        return semesterResultsInteractor.findAllByUserAndPeriod(user, period)
    }

    fun createSemesterResult(
        subjectEnrollment: SubjectEnrollment,
        user: User,
        semesterResultOutput: SemesterResultOutput
    ): SemesterResult {
        val semesterResult =
            semesterResultsInteractor.findBySubjectEnrollmentAndUser(subjectEnrollment, user)

        return semesterResultsInteractor.save(
            SemesterResult(
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
        subjectEnrollment: SubjectEnrollment,
        user: User,
        newSemesterResult: SemesterResultOutput
    ): Boolean {
        val semesterResult =
            semesterResultsInteractor.findBySubjectEnrollmentAndUser(subjectEnrollment, user)

        if (semesterResult != null) {
            return newSemesterResult.finalGrade != semesterResult.finalGrade
        }

        return false
    }
}
