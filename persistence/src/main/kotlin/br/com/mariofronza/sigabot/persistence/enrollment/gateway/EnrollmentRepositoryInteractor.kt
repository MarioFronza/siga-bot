package br.com.mariofronza.sigabot.persistence.enrollment.gateway

import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.misc.toJPA
import br.com.mariofronza.sigabot.persistence.enrollment.repository.EnrollmentRepository
import br.com.mariofronza.sigabot.usecases.siga.enrollment.gateway.EnrollmentInteractor
import org.springframework.stereotype.Component

@Component
class EnrollmentRepositoryInteractor(
    private val enrollmentRepository: EnrollmentRepository
) : EnrollmentInteractor {

    override fun findAllByUserAndPeriod(user: User, period: AcademicTerm): List<Enrollment> {
        return enrollmentRepository.findAllByUserAndPeriod(user.toJPA(), period.year, period.term).map { it.toEntity() }
    }

    override fun findBySubjectEnrollmentAndUser(subjectEnrollment: SubjectOffering, user: User): Enrollment? {
        return enrollmentRepository.findBySubjectEnrolledAndUser(subjectEnrollment.toJPA(), user.toJPA())?.toEntity()
    }

    override fun save(semesterResult: Enrollment): Enrollment {
        return enrollmentRepository.save(semesterResult.toJPA()).toEntity()
    }
}
