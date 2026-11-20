package br.com.mariofronza.sigabot.http.controller.me

import br.com.mariofronza.sigabot.usecases.misc.filter.UserDetailsImpl
import br.com.mariofronza.sigabot.usecases.retrieve.RetrieveService
import br.com.mariofronza.sigabot.usecases.retrieve.dto.RetrieveInput
import br.com.mariofronza.sigabot.usecases.retrieve.dto.SyncInput
import br.com.mariofronza.sigabot.usecases.retrieve.dto.SyncScope
import br.com.mariofronza.sigabot.usecases.siga.enrollment.EnrollmentService
import br.com.mariofronza.sigabot.usecases.siga.enrollment.dto.EnrollmentOutput
import br.com.mariofronza.sigabot.usecases.siga.term.AcademicTermService
import br.com.mariofronza.sigabot.usecases.siga.term.dto.AcademicTermOutput
import br.com.mariofronza.sigabot.usecases.user.dto.UserOutput
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/{version}/me", version = "v1")
class MeController(
    private val academicTermService: AcademicTermService,
    private val enrollmentService: EnrollmentService,
    private val retrieveService: RetrieveService,
) {

    @GetMapping
    fun show(@AuthenticationPrincipal principal: UserDetailsImpl) = UserOutput.from(principal.toUser())

    @GetMapping("/terms")
    fun terms(@AuthenticationPrincipal principal: UserDetailsImpl): List<AcademicTermOutput> =
        academicTermService.findAllEnrolledBy(principal.toUser()).map(AcademicTermOutput::from).distinct()

    @GetMapping("/terms/{year}/{term}/enrollments")
    fun enrollments(
        @AuthenticationPrincipal principal: UserDetailsImpl,
        @PathVariable year: Int,
        @PathVariable term: Int,
    ): List<EnrollmentOutput> = enrollmentService.findAll(principal.toUser(), year, term).map(EnrollmentOutput::from)

    @PostMapping("/syncs")
    fun sync(@AuthenticationPrincipal principal: UserDetailsImpl, @RequestBody input: SyncInput): List<AcademicTermOutput> {
        val user = principal.toUser()
        val (scope, year, term) = input
        if (scope == SyncScope.FULL) return retrieveService.fullRetrieve(user)
        if (year == null || term == null) throw ResponseStatusException(BAD_REQUEST, "year and term are required to sync one term")
        return retrieveService.retrieveForUser(RetrieveInput(year = year, term = term), user)
    }
}
