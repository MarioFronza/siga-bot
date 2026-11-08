package br.com.mariofronza.sigabot.usecases.siga.subject.gateway

import br.com.mariofronza.sigabot.entity.group.Group
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.entity.subject.SubjectEnrollment

interface SubjectEnrollmentInteractor {

    fun findBySubjectAndEnrolledGroup(subject: Subject, enrolledGroup: Group): SubjectEnrollment?

    fun save(subjectEnrollment: SubjectEnrollment): SubjectEnrollment
}
