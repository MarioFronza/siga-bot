package br.com.mariofronza.sigabot.usecases.siga.enrollment.gateway

import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.entity.user.User

interface EnrollmentInteractor {

    fun findAllByUserAndPeriod(user: User, period: AcademicTerm): List<Enrollment>

    fun findBySubjectEnrollmentAndUser(subjectEnrollment: SubjectOffering, user: User): Enrollment?

    fun save(semesterResult: Enrollment): Enrollment
}
