package br.com.mariofronza.sigabot.persistence.subject.repository

import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectOfferingJPA
import org.springframework.data.jpa.repository.JpaRepository

interface SubjectOfferingRepository : JpaRepository<SubjectOfferingJPA, Long> {

    fun findBySubjectIdAndClassSectionId(subjectId: Long, classSectionId: Long): SubjectOfferingJPA?
}
