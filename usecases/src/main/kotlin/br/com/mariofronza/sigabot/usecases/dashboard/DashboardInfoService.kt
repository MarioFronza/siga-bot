package br.com.mariofronza.sigabot.usecases.dashboard

import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.dashboard.dto.DashboardInput
import br.com.mariofronza.sigabot.usecases.dashboard.dto.DashboardOutput
import br.com.mariofronza.sigabot.usecases.dashboard.dto.ResultOutput
import br.com.mariofronza.sigabot.usecases.misc.filter.UserDetailsImpl
import br.com.mariofronza.sigabot.usecases.siga.term.AcademicTermService
import br.com.mariofronza.sigabot.usecases.siga.term.dto.AcademicTermOutput
import br.com.mariofronza.sigabot.usecases.siga.enrollment.EnrollmentService
import br.com.mariofronza.sigabot.usecases.user.dto.UserPeriodsOutput
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class DashboardInfoService(
    private val academicTermService: AcademicTermService,
    private val enrollmentService: EnrollmentService,
) {

    fun getDashboardInfo(): UserPeriodsOutput {
        val user = getCurrentUser()
        return UserPeriodsOutput(
            username = user.username,
            periods = academicTermService.findAllEnrolledBy(user).map(AcademicTermOutput::from)
        )
    }

    fun getStudentSemesterResults(dashboardInput: DashboardInput): DashboardOutput {
        val user = getCurrentUser()
        val academicTerm = academicTermService.findByYearAndTerm(
            year = dashboardInput.year,
            term = dashboardInput.term
        )

        val results = enrollmentService.findAll(user, academicTerm).map {
            ResultOutput(
                finalGrade = it.finalGrade,
                courseLoad = it.subjectOffering.courseLoad,
                absencesCount = it.absences,
                attendancePercentage = it.attendancePercentage,
                result = it.result,
                subjectName = it.subjectOffering.subject.name,
                groupName = it.subjectOffering.classSection.code
            )
        }

        return DashboardOutput(
            period = AcademicTermOutput.from(academicTerm),
            results = results
        )
    }

    private fun getCurrentUser(): User {
        val currentUser = SecurityContextHolder.getContext().authentication!!.principal as UserDetailsImpl
        return currentUser.toUser()
    }
}
