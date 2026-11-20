package br.com.mariofronza.sigabot.persistence.subject.repository

import br.com.mariofronza.sigabot.persistence.section.entity.ClassSectionJPA
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectOfferingJPA
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectJPA
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubjectOfferingRepository : JpaRepository<SubjectOfferingJPA, Long> {

    fun findBySubjectAndEnrolledGroup(subject: SubjectJPA, enrolledGroup: ClassSectionJPA): SubjectOfferingJPA?
}
