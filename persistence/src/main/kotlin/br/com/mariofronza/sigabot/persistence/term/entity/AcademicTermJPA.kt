package br.com.mariofronza.sigabot.persistence.term.entity

import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.persistence.institution.entity.InstitutionJPA
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
@Table(name = "academic_terms")
class AcademicTermJPA(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id")
    val institution: InstitutionJPA,
    @Column(name = "year")
    val year: Int,
    @Column(name = "term")
    val term: Int,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun toEntity() = AcademicTerm(id = id, institution = institution.toEntity(), year = year, term = term)
}
