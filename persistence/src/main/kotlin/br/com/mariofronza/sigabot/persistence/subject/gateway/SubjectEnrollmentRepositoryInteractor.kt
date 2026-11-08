package br.com.mariofronza.sigabot.persistence.subject.gateway

import br.com.mariofronza.sigabot.entity.group.Group
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.entity.subject.SubjectEnrollment
import br.com.mariofronza.sigabot.persistence.misc.toJPA
import br.com.mariofronza.sigabot.persistence.subject.repository.SubjectEnrollmentRepository
import br.com.mariofronza.sigabot.usecases.siga.subject.gateway.SubjectEnrollmentInteractor
import org.springframework.stereotype.Component

@Component
class SubjectEnrollmentRepositoryInteractor(
    private val subjectEnrollmentRepository: SubjectEnrollmentRepository
) : SubjectEnrollmentInteractor {

    override fun findBySubjectAndEnrolledGroup(subject: Subject, enrolledGroup: Group): SubjectEnrollment? {
        return subjectEnrollmentRepository.findBySubjectAndEnrolledGroup(
            subject = subject.toJPA(),
            enrolledGroup = enrolledGroup.toJPA()
        )?.toEntity()
    }

    override fun save(subjectEnrollment: SubjectEnrollment): SubjectEnrollment {
        return subjectEnrollmentRepository.save(subjectEnrollment.toJPA()).toEntity()
    }
}
