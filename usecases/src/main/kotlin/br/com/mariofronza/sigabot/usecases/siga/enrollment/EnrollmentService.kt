package br.com.mariofronza.sigabot.usecases.siga.enrollment

import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.usecases.siga.enrollment.gateway.EnrollmentInteractor
import org.springframework.stereotype.Service

@Service
class EnrollmentService(
    private val enrollmentInteractor: EnrollmentInteractor,
) {

    fun findAll(studentId: UserId, period: AcademicPeriod): List<Enrollment> = enrollmentInteractor.findAll(studentId, period)

    fun findPeriods(studentId: UserId): List<AcademicPeriod> = enrollmentInteractor.findPeriods(studentId)
}
