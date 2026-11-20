package br.com.mariofronza.sigabot.persistence.term.entity

import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "academic_terms")
class AcademicTermJPA(
    @Column(name = "year")
    val year: Int,
    @Column(name = "term")
    val term: Int,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun toEntity() = AcademicTerm(id = id, year = year, term = term)
}
