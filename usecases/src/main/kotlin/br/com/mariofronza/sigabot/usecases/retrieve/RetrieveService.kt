package br.com.mariofronza.sigabot.usecases.retrieve

import br.com.mariofronza.sigabot.entity.catalog.SubjectOffering
import br.com.mariofronza.sigabot.entity.enrollment.Attendance
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.enrollment.Grade
import br.com.mariofronza.sigabot.entity.enrollment.GradePosted
import br.com.mariofronza.sigabot.entity.enrollment.SemesterOutcome
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.credential.gateway.SecretCipher
import br.com.mariofronza.sigabot.usecases.extraction.semester.SemesterResultsExtraction
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SemesterResultOutput
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SigaCredentialInput
import br.com.mariofronza.sigabot.usecases.notification.PushNotification
import br.com.mariofronza.sigabot.usecases.retrieve.dto.RetrieveInput
import br.com.mariofronza.sigabot.usecases.siga.enrollment.gateway.EnrollmentInteractor
import br.com.mariofronza.sigabot.usecases.siga.term.dto.AcademicTermOutput
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RetrieveService(
    private val userInteractor: UserInteractor,
    private val enrollmentInteractor: EnrollmentInteractor,
    private val semesterResultsExtraction: SemesterResultsExtraction,
    private val socketNotification: PushNotification,
    private val secretCipher: SecretCipher
) {

    fun fullRetrieve(user: User) = retrieveSemesterResults(RetrieveInput(), user, completeExtraction = true)

    fun retrieveForUser(retrieveInput: RetrieveInput, user: User) = retrieveSemesterResults(retrieveInput, user)

    private fun retrieveSemesterResults(
        retrieveInput: RetrieveInput,
        currentUser: User,
        completeExtraction: Boolean = false,
    ): List<AcademicTermOutput> {
        logger.debug("Retrieving SIGA results for user {}", currentUser.id)

        val account = currentUser.accountAt(InstitutionCode.UDESC)
        val input = retrieveInput.toSemesterResultsExtractionInput(generateSigaCredentialInput(account))
        val semesterResultsList = semesterResultsExtraction.extract(input, completeExtraction = completeExtraction)

        val user = currentUser.rename(semesterResultsList.studentName)
        userInteractor.save(user)

        val periods = semesterResultsList.semesterResultsPeriod.map { semesterResults ->
            val period = AcademicPeriod.parse(semesterResults.period)
            semesterResults.semesterResults.forEach {
                val offering = offeringOf(account, period, semesterResultsList.course, it)
                val recorded = Enrollment.record(user.id, offering, enrollmentInteractor.find(user.id, offering), outcomeOf(it))
                enrollmentInteractor.save(recorded.enrollment)
                recorded.events.forEach { event -> notifyWatchers(user, event) }
            }
            period
        }

        // return updated subject here
        return periods.map(AcademicTermOutput::from)
    }

    private fun offeringOf(account: AcademicAccount, period: AcademicPeriod, course: String, result: SemesterResultOutput) =
        SubjectOffering(
            institution = account.institution,
            period = period,
            course = course,
            classSection = result.groupName,
            subject = result.subjectName,
            courseLoad = result.courseLoad,
        )

    private fun outcomeOf(result: SemesterResultOutput) = SemesterOutcome(
        grade = Grade(result.finalGrade),
        absences = result.absencesCount,
        attendance = Attendance(result.attendancePercentage),
        result = result.result,
    )

    private fun notifyWatchers(user: User, event: GradePosted) {
        logger.info("New grade for user {} in {}", user.id, event.offering.subject)
        val message = "${user.username}, você possui uma nota nova da matéria ${event.offering.subject}"
        socketNotification.notifyUser(user.id, message)
    }

    private fun generateSigaCredentialInput(account: AcademicAccount): SigaCredentialInput {
        return SigaCredentialInput(
            cpf = secretCipher.decrypt(account.loginCiphertext),
            password = secretCipher.decrypt(account.passwordCiphertext)
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RetrieveService::class.java)
    }
}
