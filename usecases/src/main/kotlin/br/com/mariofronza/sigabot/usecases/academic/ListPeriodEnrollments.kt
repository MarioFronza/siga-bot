package br.com.mariofronza.sigabot.usecases.academic

import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.usecases.academic.port.EnrollmentRepository

class ListPeriodEnrollments(private val enrollments: EnrollmentRepository) {

    operator fun invoke(studentId: UserId, period: AcademicPeriod): List<Enrollment> = enrollments.findAll(studentId, period)
}
