package br.com.mariofronza.sigabot.usecases.siga.subject

import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.usecases.siga.subject.gateway.SubjectOfferingInteractor
import org.springframework.stereotype.Service

@Service
class SubjectOfferingService(
    private val subjectOfferingInteractor: SubjectOfferingInteractor
) {

    fun createSubjectEnrollment(subject: Subject, group: ClassSection): SubjectOffering {
        var subjectEnrollment = subjectOfferingInteractor.findBySubjectAndEnrolledGroup(subject, group)

        if (subjectEnrollment == null) {
            subjectEnrollment = subjectOfferingInteractor.save(
                SubjectOffering(
                    subject = subject,
                    enrolledGroup = group
                )
            )
        }

        return subjectEnrollment
    }
}
