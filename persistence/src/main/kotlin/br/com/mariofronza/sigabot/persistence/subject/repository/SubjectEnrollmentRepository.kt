package br.com.mariofronza.sigabot.persistence.subject.repository

import br.com.mariofronza.sigabot.persistence.group.entity.GroupJPA
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectEnrollmentJPA
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectJPA
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubjectEnrollmentRepository : JpaRepository<SubjectEnrollmentJPA, Long> {

    fun findBySubjectAndEnrolledGroup(subject: SubjectJPA, enrolledGroup: GroupJPA): SubjectEnrollmentJPA?
}
