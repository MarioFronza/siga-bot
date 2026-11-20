package br.com.mariofronza.sigabot.usecases.retrieve

import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.siga.course.CourseService
import br.com.mariofronza.sigabot.usecases.extraction.semester.SemesterResultsExtraction
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SigaCredentialInput
import br.com.mariofronza.sigabot.usecases.misc.cryptograpy.CryptographyService
import br.com.mariofronza.sigabot.usecases.misc.filter.UserDetailsImpl
import br.com.mariofronza.sigabot.usecases.siga.section.ClassSectionService
import br.com.mariofronza.sigabot.usecases.siga.term.AcademicTermService
import br.com.mariofronza.sigabot.usecases.siga.term.dto.AcademicTermOutput
import br.com.mariofronza.sigabot.usecases.notification.PushNotification
import br.com.mariofronza.sigabot.usecases.retrieve.dto.RetrieveInput
import br.com.mariofronza.sigabot.usecases.retrieve.dto.RetrieveOutput
import br.com.mariofronza.sigabot.usecases.siga.enrollment.EnrollmentService
import br.com.mariofronza.sigabot.usecases.siga.subject.SubjectOfferingService
import br.com.mariofronza.sigabot.usecases.siga.subject.SubjectService
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class RetrieveService(
    private val userInteractor: UserInteractor,
    private val courseService: CourseService,
    private val academicTermService: AcademicTermService,
    private val classSectionService: ClassSectionService,
    private val subjectService: SubjectService,
    private val subjectOfferingService: SubjectOfferingService,
    private val enrollmentService: EnrollmentService,
    private val semesterResultsExtraction: SemesterResultsExtraction,
    private val socketNotification: PushNotification,
    private val cryptographyService: CryptographyService
) {

    fun retrieve(retrieveInput: RetrieveInput) = retrieveSemesterResults(retrieveInput)

    fun fullRetrieve(retrieveInput: RetrieveInput) = retrieveSemesterResults(retrieveInput, true)

    fun retrieveForUser(retrieveInput: RetrieveInput, user: User) =
        retrieveSemesterResults(retrieveInput = retrieveInput, currentUser = user)

    private fun retrieveSemesterResults(
        retrieveInput: RetrieveInput,
        completeExtraction: Boolean = false,
        currentUser: User? = null
    ): RetrieveOutput {
        var user = currentUser ?: getCurrentUser()

        logger.debug("Retrieving SIGA results for user {}", user.externalId)

        // The scraper only speaks UDESC's SIGA so far.
        val account = user.academicAccounts.first { it.institution.code == Institution.UDESC }
        val institution = account.institution

        val input = retrieveInput.toSemesterResultsExtractionInput(generateSigaCredentialInput(account))

        val semesterResultsList = semesterResultsExtraction.extract(input, completeExtraction = completeExtraction)

        val course = courseService.findOrCreate(institution, semesterResultsList.course)
        user = userInteractor.save(user.copy(fullName = semesterResultsList.studentName))
        val academicTerms = mutableListOf<AcademicTerm>()

        semesterResultsList.semesterResultsPeriod.map { semesterResults ->
            val (year, term) = parseAcademicTerm(semesterResults.period)
            val academicTerm = academicTermService.findOrCreate(institution, year, term)
            academicTerms.add(academicTerm)

            semesterResults.semesterResults.map {
                val classSection = classSectionService.findOrCreate(it.groupName, course, academicTerm)
                val subject = subjectService.findOrCreate(institution, it.subjectName)
                val subjectOffering = subjectOfferingService.findOrCreate(subject, classSection, it.courseLoad)

                if (enrollmentService.hasGradeChanged(user, subjectOffering, it)) {
                    logger.info("New grade for user {} in {}", user.externalId, subject.name)
                    notifyWatchers(user, subject.name)
                }

                enrollmentService.record(user, subjectOffering, it)
            }
        }

        // return updated subject here
        return RetrieveOutput(
            username = user.username,
            periods = academicTerms.map(AcademicTermOutput::from)
        )
    }

    private fun parseAcademicTerm(text: String): Pair<Int, Int> {
        val (year, term) = text.split("/")
        return Pair(year.toInt(), term.toInt())
    }

    private fun notifyWatchers(user: User, subjectName: String) {
        val message = "${user.username}, você possui uma nota nova da matéria $subjectName"
        socketNotification.notifyUser(user.externalId!!, message)
    }

    private fun getCurrentUser(): User {
        val currentUser = SecurityContextHolder.getContext().authentication!!.principal as UserDetailsImpl
        return currentUser.toUser()
    }

    private fun generateSigaCredentialInput(account: AcademicAccount): SigaCredentialInput {
        return SigaCredentialInput(
            cpf = cryptographyService.decrypt(account.loginCiphertext),
            password = cryptographyService.decrypt(account.passwordCiphertext)
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RetrieveService::class.java)
    }
}
