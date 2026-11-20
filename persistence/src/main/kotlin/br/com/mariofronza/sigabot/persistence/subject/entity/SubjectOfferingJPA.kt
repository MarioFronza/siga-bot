package br.com.mariofronza.sigabot.persistence.subject.entity

import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.persistence.section.entity.ClassSectionJPA
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "subject_offerings")
class SubjectOfferingJPA(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_section_id")
    val classSection: ClassSectionJPA,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id")
    val subject: SubjectJPA,
    @Column(name = "course_load")
    var courseLoad: Int,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun toEntity() = SubjectOffering(
        id = id,
        subject = subject.toEntity(),
        classSection = classSection.toEntity(),
        courseLoad = courseLoad,
    )
}
