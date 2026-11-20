package br.com.mariofronza.sigabot.persistence.term.repository

import br.com.mariofronza.sigabot.persistence.term.entity.AcademicTermJPA
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface AcademicTermRepository : JpaRepository<AcademicTermJPA, Long> {

    fun findByYearAndTerm(year: Int, term: Int): AcademicTermJPA?

    @Query(
        """
        select distinct t from EnrollmentJPA e
        join e.subjectOffering.classSection.academicTerm t
        where e.user.id = :userId
        order by t.year desc, t.term desc
        """
    )
    fun findAllEnrolledBy(userId: Long): List<AcademicTermJPA>
}
