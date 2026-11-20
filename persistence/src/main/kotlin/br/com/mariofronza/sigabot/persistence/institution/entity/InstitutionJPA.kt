package br.com.mariofronza.sigabot.persistence.institution.entity

import br.com.mariofronza.sigabot.entity.institution.Institution
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "institutions")
class InstitutionJPA(
    @Column(name = "code")
    val code: String,
    @Column(name = "name")
    val name: String,
    @Column(name = "grade_scale_max", precision = 5, scale = 2)
    val gradeScaleMax: BigDecimal,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun toEntity() = Institution(id = id, code = code, name = name, gradeScaleMax = gradeScaleMax.toDouble())
}
