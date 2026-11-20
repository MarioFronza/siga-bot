package br.com.mariofronza.sigabot.persistence.term.entity

import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import jakarta.persistence.*

@Entity
@Table(name = "periods")
data class AcademicTermJPA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "year")
    val year: String,
    @Column(name = "term")
    val term: Int
) {
    fun toEntity() = AcademicTerm(
        id = id,
        year = year,
        term = term
    )
}
