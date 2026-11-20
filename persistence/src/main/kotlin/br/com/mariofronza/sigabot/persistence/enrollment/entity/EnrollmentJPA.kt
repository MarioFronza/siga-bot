package br.com.mariofronza.sigabot.persistence.enrollment.entity

import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectOfferingJPA
import br.com.mariofronza.sigabot.persistence.user.entity.UserJPA
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "enrollments")
class EnrollmentJPA(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    val user: UserJPA,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_offering_id")
    val subjectOffering: SubjectOfferingJPA,
    @Column(name = "final_grade", precision = 4, scale = 2)
    var finalGrade: BigDecimal?,
    @Column(name = "absences")
    var absences: Int,
    @Column(name = "attendance_percentage", precision = 5, scale = 2)
    var attendancePercentage: BigDecimal?,
    @Column(name = "result")
    var result: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: Instant? = null

    fun toEntity(user: User) = Enrollment(
        id = id,
        user = user,
        subjectOffering = subjectOffering.toEntity(),
        finalGrade = finalGrade?.toDouble() ?: 0.0,
        absences = absences,
        attendancePercentage = attendancePercentage?.toDouble() ?: 0.0,
        result = result,
    )
}
