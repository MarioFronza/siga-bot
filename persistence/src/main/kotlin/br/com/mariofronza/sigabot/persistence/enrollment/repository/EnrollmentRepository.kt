package br.com.mariofronza.sigabot.persistence.enrollment.repository

import br.com.mariofronza.sigabot.persistence.enrollment.entity.EnrollmentJPA
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectOfferingJPA
import br.com.mariofronza.sigabot.persistence.user.entity.UserJPA
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface EnrollmentRepository : JpaRepository<EnrollmentJPA, Long> {

    @Query(
        "select sr from EnrollmentJPA sr " +
                "inner join SubjectOfferingJPA se on sr.subjectEnrolled = se and sr.user = ?1 " +
                "inner join ClassSectionJPA g on se.enrolledGroup = g " +
                "inner join AcademicTermJPA p on g.period = p and g.period.year = ?2 and g.period.term = ?3"
    )
    fun findAllByUserAndPeriod(user: UserJPA, year: String, term: Int): List<EnrollmentJPA>

    fun findBySubjectEnrolledAndUser(
        subjectEnrollment: SubjectOfferingJPA,
        user: UserJPA
    ): EnrollmentJPA?
}
