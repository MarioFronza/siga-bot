package br.com.mariofronza.sigabot.persistence.section.repository

import br.com.mariofronza.sigabot.persistence.section.entity.ClassSectionJPA
import org.springframework.data.jpa.repository.JpaRepository

interface ClassSectionRepository : JpaRepository<ClassSectionJPA, Long> {

    fun findByCodeAndCourseIdAndAcademicTermId(code: String, courseId: Long, academicTermId: Long): ClassSectionJPA?
}
