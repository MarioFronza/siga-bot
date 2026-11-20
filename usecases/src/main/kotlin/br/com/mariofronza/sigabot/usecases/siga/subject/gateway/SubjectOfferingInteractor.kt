package br.com.mariofronza.sigabot.usecases.siga.subject.gateway

import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.entity.subject.SubjectOffering

interface SubjectOfferingInteractor {

    fun findBySubjectAndEnrolledGroup(subject: Subject, enrolledGroup: ClassSection): SubjectOffering?

    fun save(subjectEnrollment: SubjectOffering): SubjectOffering
}
