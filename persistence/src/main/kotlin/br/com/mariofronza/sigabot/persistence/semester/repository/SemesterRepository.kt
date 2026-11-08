package br.com.mariofronza.sigabot.persistence.semester.repository

import br.com.mariofronza.sigabot.persistence.semester.entity.SemesterResultJPA
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectEnrollmentJPA
import br.com.mariofronza.sigabot.persistence.user.entity.UserJPA
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SemesterRepository : JpaRepository<SemesterResultJPA, Long> {

    @Query(
        "select sr from SemesterResultJPA sr " +
                "inner join SubjectEnrollmentJPA se on sr.subjectEnrolled = se and sr.user = ?1 " +
                "inner join GroupJPA g on se.enrolledGroup = g " +
                "inner join PeriodJPA p on g.period = p and g.period.year = ?2 and g.period.term = ?3"
    )
    fun findAllByUserAndPeriod(user: UserJPA, year: String, term: Int): List<SemesterResultJPA>

    fun findBySubjectEnrolledAndUser(
        subjectEnrollment: SubjectEnrollmentJPA,
        user: UserJPA
    ): SemesterResultJPA?
}
