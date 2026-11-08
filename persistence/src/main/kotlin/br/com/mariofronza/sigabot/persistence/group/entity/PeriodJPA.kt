package br.com.mariofronza.sigabot.persistence.group.entity

import br.com.mariofronza.sigabot.entity.group.Period
import jakarta.persistence.*

@Entity
@Table(name = "periods")
data class PeriodJPA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "year")
    val year: String,
    @Column(name = "term")
    val term: Int
) {
    fun toEntity() = Period(
        id = id,
        year = year,
        term = term
    )
}
