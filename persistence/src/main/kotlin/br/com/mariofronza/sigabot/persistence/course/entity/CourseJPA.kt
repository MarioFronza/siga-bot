package br.com.mariofronza.sigabot.persistence.course.entity

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.persistence.group.entity.GroupJPA
import jakarta.persistence.*

@Entity
@Table(name = "courses")
data class CourseJPA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "name")
    val name: String,
    @OneToMany(mappedBy = "course")
    val groups: List<GroupJPA> = emptyList()
) {
    fun toEntity() = Course(
        id = id,
        name = name
    )
}
