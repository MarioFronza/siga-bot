package br.com.mariofronza.sigabot.usecases.siga.enrollment.gateway

import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.entity.user.User

interface EnrollmentInteractor {

    fun findAll(user: User, period: AcademicPeriod): List<Enrollment>

    fun find(user: User, subjectOffering: SubjectOffering): Enrollment?

    fun save(enrollment: Enrollment): Enrollment
}
