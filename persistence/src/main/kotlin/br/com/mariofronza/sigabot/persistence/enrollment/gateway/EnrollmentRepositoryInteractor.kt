package br.com.mariofronza.sigabot.persistence.enrollment.gateway

import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.enrollment.entity.EnrollmentJPA
import br.com.mariofronza.sigabot.persistence.enrollment.repository.EnrollmentRepository
import br.com.mariofronza.sigabot.persistence.subject.repository.SubjectOfferingRepository
import br.com.mariofronza.sigabot.persistence.user.repository.UserRepository
import br.com.mariofronza.sigabot.usecases.siga.enrollment.gateway.EnrollmentInteractor
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
@Transactional
class EnrollmentRepositoryInteractor(
    private val enrollmentRepository: EnrollmentRepository,
    private val userRepository: UserRepository,
    private val subjectOfferingRepository: SubjectOfferingRepository,
) : EnrollmentInteractor {

    override fun findAll(user: User, academicTerm: AcademicTerm): List<Enrollment> =
        enrollmentRepository.findAllByUserIdAndAcademicTermId(user.id!!, academicTerm.id!!).map { it.toEntity(user) }

    override fun find(user: User, subjectOffering: SubjectOffering): Enrollment? =
        enrollmentRepository.findByUserIdAndSubjectOfferingId(user.id!!, subjectOffering.id!!)
            ?.let { it.toEntity(user).copy(subjectOffering = subjectOffering) }

    override fun save(enrollment: Enrollment): Enrollment {
        val saved = enrollment.id
            ?.let { enrollmentRepository.findById(it).orElseThrow().apply { update(enrollment) } }
            ?: enrollmentRepository.save(create(enrollment))
        return enrollment.copy(id = saved.id)
    }

    private fun EnrollmentJPA.update(enrollment: Enrollment) {
        finalGrade = BigDecimal.valueOf(enrollment.finalGrade)
        absences = enrollment.absences
        attendancePercentage = BigDecimal.valueOf(enrollment.attendancePercentage)
        result = enrollment.result
    }

    private fun create(enrollment: Enrollment) = EnrollmentJPA(
        user = userRepository.getReferenceById(enrollment.user.id!!),
        subjectOffering = subjectOfferingRepository.getReferenceById(enrollment.subjectOffering.id!!),
        finalGrade = BigDecimal.valueOf(enrollment.finalGrade),
        absences = enrollment.absences,
        attendancePercentage = BigDecimal.valueOf(enrollment.attendancePercentage),
        result = enrollment.result,
    )
}
