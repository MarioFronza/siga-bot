package br.com.mariofronza.sigabot.usecases.dashboard

import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.dashboard.dto.DashboardInput
import br.com.mariofronza.sigabot.usecases.dashboard.dto.DashboardOutput
import br.com.mariofronza.sigabot.usecases.dashboard.dto.ResultOutput
import br.com.mariofronza.sigabot.usecases.misc.filter.UserDetailsImpl
import br.com.mariofronza.sigabot.usecases.siga.group.PeriodService
import br.com.mariofronza.sigabot.usecases.siga.semester.SemesterResultsService
import br.com.mariofronza.sigabot.usecases.user.UserService
import br.com.mariofronza.sigabot.usecases.user.dto.UserPeriodsOutput
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class DashboardInfoService(
    private val periodService: PeriodService,
    private val userService: UserService,
    private val semesterResultsService: SemesterResultsService,
) {

    fun getDashboardInfo(): UserPeriodsOutput {
        return userService.findUserPeriods()
    }

    fun getStudentSemesterResults(dashboardInput: DashboardInput): DashboardOutput {
        val user = getCurrentUser()
        val period = periodService.findByYearAndTerm(
            year = dashboardInput.year,
            term = dashboardInput.term
        )

        val results = semesterResultsService.listAllByUserAndPeriod(user, period).map {
            ResultOutput(
                finalGrade = it.finalGrade,
                courseLoad = it.courseLoad,
                absencesCount = it.absencesCount,
                attendancePercentage = it.attendancePercentage,
                result = it.result,
                subjectName = it.subjectEnrolled.subject.name,
                groupName = it.subjectEnrolled.enrolledGroup.name
            )
        }

        return DashboardOutput(
            period = period,
            results = results
        )
    }

    private fun getCurrentUser(): User {
        val currentUser = SecurityContextHolder.getContext().authentication!!.principal as UserDetailsImpl
        return currentUser.toUser()
    }
}
