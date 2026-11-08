package br.com.mariofronza.sigabot.usecases.siga.subject

import br.com.mariofronza.sigabot.entity.group.Group
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.entity.subject.SubjectEnrollment
import br.com.mariofronza.sigabot.usecases.siga.subject.gateway.SubjectEnrollmentInteractor
import org.springframework.stereotype.Service

@Service
class SubjectEnrollmentService(
    private val subjectEnrollmentInteractor: SubjectEnrollmentInteractor
) {

    fun createSubjectEnrollment(subject: Subject, group: Group): SubjectEnrollment {
        var subjectEnrollment = subjectEnrollmentInteractor.findBySubjectAndEnrolledGroup(subject, group)

        if (subjectEnrollment == null) {
            subjectEnrollment = subjectEnrollmentInteractor.save(
                SubjectEnrollment(
                    subject = subject,
                    enrolledGroup = group
                )
            )
        }

        return subjectEnrollment
    }
}
