package br.com.mariofronza.sigabot.persistence.course.repository

import br.com.mariofronza.sigabot.persistence.course.entity.CourseJPA
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CourseRepository : JpaRepository<CourseJPA, Long> {

    fun findByName(name: String): CourseJPA?
}
