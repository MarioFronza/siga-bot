package br.com.mariofronza.sigabot.persistence.course.entity

import br.com.mariofronza.sigabot.entity.course.Course
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "courses")
class CourseJPA(
    @Column(name = "name")
    val name: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun toEntity() = Course(id = id, name = name)
}
