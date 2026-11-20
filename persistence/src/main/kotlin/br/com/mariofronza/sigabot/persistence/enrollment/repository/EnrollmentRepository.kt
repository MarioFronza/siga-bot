package br.com.mariofronza.sigabot.persistence.enrollment.repository

import br.com.mariofronza.sigabot.persistence.enrollment.entity.EnrollmentJPA
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface EnrollmentRepository : JpaRepository<EnrollmentJPA, Long> {

    fun findByUserIdAndSubjectOfferingId(userId: Long, subjectOfferingId: Long): EnrollmentJPA?

    @Query(
        """
        select e from EnrollmentJPA e
        join fetch e.subjectOffering o
        join fetch o.subject
        join fetch o.classSection s
        join fetch s.course
        join fetch s.academicTerm t
        where e.user.id = :userId and t.id = :academicTermId
        """
    )
    fun findAllByUserIdAndAcademicTermId(userId: Long, academicTermId: Long): List<EnrollmentJPA>
}
