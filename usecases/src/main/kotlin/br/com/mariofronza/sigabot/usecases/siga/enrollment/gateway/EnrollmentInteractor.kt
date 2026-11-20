package br.com.mariofronza.sigabot.usecases.siga.enrollment.gateway

import br.com.mariofronza.sigabot.entity.catalog.SubjectOffering
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.user.UserId

interface EnrollmentInteractor {

    fun find(studentId: UserId, offering: SubjectOffering): Enrollment?

    fun findAll(studentId: UserId, period: AcademicPeriod): List<Enrollment>

    fun findPeriods(studentId: UserId): List<AcademicPeriod>

    fun save(enrollment: Enrollment)
}
