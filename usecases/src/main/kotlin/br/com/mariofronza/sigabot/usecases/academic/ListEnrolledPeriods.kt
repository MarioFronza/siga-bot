package br.com.mariofronza.sigabot.usecases.academic

import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.usecases.academic.port.EnrollmentRepository

class ListEnrolledPeriods(private val enrollments: EnrollmentRepository) {

    operator fun invoke(studentId: UserId): List<AcademicPeriod> = enrollments.findPeriods(studentId)
}
