package br.com.mariofronza.sigabot.http.controller.me

import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.http.controller.EnrollmentResponse
import br.com.mariofronza.sigabot.http.controller.PeriodResponse
import br.com.mariofronza.sigabot.http.controller.UserResponse
import br.com.mariofronza.sigabot.http.security.UserDetailsImpl
import br.com.mariofronza.sigabot.usecases.academic.ListEnrolledPeriods
import br.com.mariofronza.sigabot.usecases.academic.ListPeriodEnrollments
import br.com.mariofronza.sigabot.usecases.academic.ShowSyncStatus
import br.com.mariofronza.sigabot.usecases.academic.RequestSync
import br.com.mariofronza.sigabot.usecases.academic.port.SyncStatus
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/{version}/me", version = "v1")
class MeController(
    private val listEnrolledPeriods: ListEnrolledPeriods,
    private val listPeriodEnrollments: ListPeriodEnrollments,
    private val requestSync: RequestSync,
    private val showSyncStatus: ShowSyncStatus,
) {

    @GetMapping
    fun show(@AuthenticationPrincipal principal: UserDetailsImpl) = UserResponse.from(principal.toUser())

    @GetMapping("/terms")
    fun terms(@AuthenticationPrincipal principal: UserDetailsImpl): List<PeriodResponse> =
        listEnrolledPeriods(principal.toUser().id).map(PeriodResponse::from)

    @GetMapping("/terms/{year}/{term}/enrollments")
    fun enrollments(
        @AuthenticationPrincipal principal: UserDetailsImpl,
        @PathVariable year: Int,
        @PathVariable term: Int,
    ): List<EnrollmentResponse> = listPeriodEnrollments(principal.toUser().id, AcademicPeriod(year, term)).map(EnrollmentResponse::from)

    @GetMapping("/sync")
    fun syncStatus(@AuthenticationPrincipal principal: UserDetailsImpl): SyncStatus =
        showSyncStatus(principal.toUser().id) ?: throw ResponseStatusException(NOT_FOUND, "No sync is scheduled for this student")

    @PostMapping("/syncs")
    fun sync(@AuthenticationPrincipal principal: UserDetailsImpl, @RequestBody input: SyncInput): ResponseEntity<Unit> {
        requestSync(principal.toUser().id, scopeOf(input))
        val status = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/me/sync").build().toUri()
        return ResponseEntity.accepted().location(status).build()
    }

    private fun scopeOf(input: SyncInput): SyncScope {
        val (scope, year, term) = input
        if (scope == SyncInput.Scope.FULL) return SyncScope.Everything
        if (year == null || term == null) throw ResponseStatusException(BAD_REQUEST, "year and term are required to sync one term")
        return SyncScope.Period(AcademicPeriod(year, term))
    }
}
