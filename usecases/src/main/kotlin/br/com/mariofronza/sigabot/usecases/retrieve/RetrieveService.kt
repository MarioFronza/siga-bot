package br.com.mariofronza.sigabot.usecases.retrieve

import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.entity.user.SigaCredential
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.siga.course.CourseService
import br.com.mariofronza.sigabot.usecases.extraction.semester.SemesterResultsExtraction
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SigaCredentialInput
import br.com.mariofronza.sigabot.usecases.misc.cryptograpy.CryptographyService
import br.com.mariofronza.sigabot.usecases.misc.filter.UserDetailsImpl
import br.com.mariofronza.sigabot.usecases.siga.section.ClassSectionService
import br.com.mariofronza.sigabot.usecases.siga.term.AcademicTermService
import br.com.mariofronza.sigabot.usecases.notification.PushNotification
import br.com.mariofronza.sigabot.usecases.retrieve.dto.RetrieveInput
import br.com.mariofronza.sigabot.usecases.retrieve.dto.RetrieveOutput
import br.com.mariofronza.sigabot.usecases.siga.enrollment.EnrollmentService
import br.com.mariofronza.sigabot.usecases.siga.subject.SubjectOfferingService
import br.com.mariofronza.sigabot.usecases.siga.subject.SubjectService
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
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
        println("Current user: ${currentUser?.username}")

        var user = currentUser ?: getCurrentUser()

        println("User: ${user.username}")

        val input = retrieveInput.toSemesterResultsExtractionInput(
            generateSigaCredentialInput(user.sigaCredential)
        )

        val semesterResultsList = semesterResultsExtraction.extract(input, completeExtraction = completeExtraction)

        val course = courseService.createCourse(semesterResultsList.course)
        user = userInteractor.save(user.copy(fullName = semesterResultsList.studentName))
        val periods = mutableListOf<AcademicTerm>()

        semesterResultsList.semesterResultsPeriod.map { semesterResults ->
            val pairPeriod = getPairPeriod(semesterResults.period)
            val period = academicTermService.createPeriod(pairPeriod.first, pairPeriod.second)
            periods.add(period)

            semesterResults.semesterResults.map {
                val group = classSectionService.createGroup(it.groupName, course, period)
                addGroupToUser(user, group)

                val subject = subjectService.createSubject(it.subjectName)
                val subjectEnrollment = subjectOfferingService.createSubjectEnrollment(
                    subject = subject,
                    group = group
                )

                if (enrollmentService.isThereAnyGradeUpdate(subjectEnrollment, user, it)) {
                    println("New grade update for ${user.username}")
                    notifyWatchers(user, subject.name)
                } else {
                    println("User ${user.username} does not have any gradle update")
                }

                enrollmentService.createSemesterResult(subjectEnrollment, user, it)
            }
        }

        // return updated subject here
        return RetrieveOutput(
            username = user.username,
            periods = periods
        )
    }

    private fun getPairPeriod(periodText: String): Pair<String, Int> {
        val (year, term) = periodText.split("/")
        return Pair(year, term.toInt())
    }

    // TODO remove that method to a better place
    private fun addGroupToUser(user: User, group: ClassSection) {
        if (user.groups.find { it.id == group.id } == null) {
            user.groups.add(group)
            userInteractor.save(user)
        }
    }

    private fun notifyWatchers(user: User, subjectName: String) {
        val message = "${user.username}, você possui uma nota nova da matéria $subjectName"
        println(message)
        socketNotification.notifyUser(user.id!!, message)
    }

    private fun getCurrentUser(): User {
        val currentUser = SecurityContextHolder.getContext().authentication!!.principal as UserDetailsImpl
        return currentUser.toUser()
    }

    private fun generateSigaCredentialInput(sigaCredential: SigaCredential): SigaCredentialInput {
        return SigaCredentialInput(
            cpf = cryptographyService.decrypt(sigaCredential.cpfHash),
            password = cryptographyService.decrypt(sigaCredential.passwordHash)
        )
    }
}
