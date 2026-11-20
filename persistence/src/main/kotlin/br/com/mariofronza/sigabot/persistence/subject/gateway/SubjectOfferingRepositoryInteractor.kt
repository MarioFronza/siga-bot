package br.com.mariofronza.sigabot.persistence.subject.gateway

import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.persistence.misc.toJPA
import br.com.mariofronza.sigabot.persistence.subject.repository.SubjectOfferingRepository
import br.com.mariofronza.sigabot.usecases.siga.subject.gateway.SubjectOfferingInteractor
import org.springframework.stereotype.Component

@Component
class SubjectOfferingRepositoryInteractor(
    private val subjectOfferingRepository: SubjectOfferingRepository
) : SubjectOfferingInteractor {

    override fun findBySubjectAndEnrolledGroup(subject: Subject, enrolledGroup: ClassSection): SubjectOffering? {
        return subjectOfferingRepository.findBySubjectAndEnrolledGroup(
            subject = subject.toJPA(),
            enrolledGroup = enrolledGroup.toJPA()
        )?.toEntity()
    }

    override fun save(subjectEnrollment: SubjectOffering): SubjectOffering {
        return subjectOfferingRepository.save(subjectEnrollment.toJPA()).toEntity()
    }
}
