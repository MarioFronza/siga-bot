package br.com.mariofronza.sigabot.http.controller.dashboard

import br.com.mariofronza.sigabot.usecases.dashboard.DashboardInfoService
import br.com.mariofronza.sigabot.usecases.dashboard.dto.DashboardInput
import br.com.mariofronza.sigabot.usecases.dashboard.dto.DashboardOutput
import br.com.mariofronza.sigabot.usecases.user.dto.UserPeriodsOutput
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/dashboard")
class DashboardController(
    private val dashboardInfoService: DashboardInfoService
) {

    @GetMapping
    fun show(
        @RequestParam year: String,
        @RequestParam term: String
    ): DashboardOutput {
        return dashboardInfoService.getStudentSemesterResults(
            DashboardInput(
                year = year,
                term = term.toInt()
            )
        )
    }

    @GetMapping("/info")
    fun info(): UserPeriodsOutput {
        return dashboardInfoService.getDashboardInfo()
    }
}
