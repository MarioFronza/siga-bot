package br.com.mariofronza.sigabot.persistence.section.repository

import br.com.mariofronza.sigabot.persistence.section.entity.ClassSectionJPA
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClassSectionRepository : JpaRepository<ClassSectionJPA, Long> {

    fun findByNameAndCourseIdAndPeriodId(name: String, courseId: Long, periodId: Long): ClassSectionJPA?
}
