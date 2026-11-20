package br.com.mariofronza.sigabot.persistence.enrollment.gateway

import br.com.mariofronza.sigabot.entity.enrollment.Grade
import br.com.mariofronza.sigabot.entity.enrollment.Attendance
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.persistence.user.gateway.userKey
import br.com.mariofronza.sigabot.persistence.notNull
import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.institution.gateway.toInstitution
import br.com.mariofronza.sigabot.persistence.jooq.tables.records.EnrollmentsRecord
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.ACADEMIC_TERMS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.CLASS_SECTIONS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.COURSES
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.ENROLLMENTS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.INSTITUTIONS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.SUBJECTS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.SUBJECT_OFFERINGS
import br.com.mariofronza.sigabot.usecases.siga.enrollment.gateway.EnrollmentInteractor
import org.jooq.DSLContext
import org.jooq.impl.DSL.currentOffsetDateTime
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
@Transactional
class EnrollmentRepositoryInteractor(
    private val dsl: DSLContext,
) : EnrollmentInteractor {

    override fun findAll(user: User, period: AcademicPeriod): List<Enrollment> =
        dsl.select(ENROLLMENTS, SUBJECT_OFFERINGS, SUBJECTS, CLASS_SECTIONS, COURSES, ACADEMIC_TERMS, INSTITUTIONS)
            .from(ENROLLMENTS)
            .join(SUBJECT_OFFERINGS).on(SUBJECT_OFFERINGS.ID.eq(ENROLLMENTS.SUBJECT_OFFERING_ID))
            .join(SUBJECTS).on(SUBJECTS.ID.eq(SUBJECT_OFFERINGS.SUBJECT_ID))
            .join(CLASS_SECTIONS).on(CLASS_SECTIONS.ID.eq(SUBJECT_OFFERINGS.CLASS_SECTION_ID))
            .join(COURSES).on(COURSES.ID.eq(CLASS_SECTIONS.COURSE_ID))
            .join(ACADEMIC_TERMS).on(ACADEMIC_TERMS.ID.eq(CLASS_SECTIONS.ACADEMIC_TERM_ID))
            .join(INSTITUTIONS).on(INSTITUTIONS.ID.eq(ACADEMIC_TERMS.INSTITUTION_ID))
            .where(ENROLLMENTS.USER_ID.eq(userKey(user.id)).and(ACADEMIC_TERMS.YEAR.eq(period.year)).and(ACADEMIC_TERMS.TERM.eq(period.term)))
            .orderBy(ENROLLMENTS.ID)
            .fetch { (enrollment, offering, subject, section, course, academicTerm, institution) ->
                val owner = institution.toInstitution()
                val classSection = ClassSection(
                    id = section.id,
                    code = section.code,
                    course = Course(id = course.id, institution = owner, name = course.name),
                    academicTerm = AcademicTerm(id = academicTerm.id, institution = owner, period = AcademicPeriod(academicTerm.year, academicTerm.term)),
                )
                enrollment.toEnrollment(
                    user,
                    SubjectOffering(
                        id = offering.id,
                        subject = Subject(id = subject.id, institution = owner, name = subject.name),
                        classSection = classSection,
                        courseLoad = offering.courseLoad,
                    ),
                )
            }

    override fun find(user: User, subjectOffering: SubjectOffering): Enrollment? =
        dsl.selectFrom(ENROLLMENTS)
            .where(ENROLLMENTS.USER_ID.eq(userKey(user.id)).and(ENROLLMENTS.SUBJECT_OFFERING_ID.eq(subjectOffering.id)))
            .fetchOne()
            ?.toEnrollment(user, subjectOffering)

    override fun save(enrollment: Enrollment): Enrollment = enrollment.id?.let { update(enrollment) } ?: insert(enrollment)

    private fun update(enrollment: Enrollment): Enrollment {
        dsl.update(ENROLLMENTS)
            .set(ENROLLMENTS.FINAL_GRADE, BigDecimal.valueOf(enrollment.finalGrade.value))
            .set(ENROLLMENTS.ABSENCES, enrollment.absences)
            .set(ENROLLMENTS.ATTENDANCE_PERCENTAGE, BigDecimal.valueOf(enrollment.attendance.percentage))
            .set(ENROLLMENTS.RESULT, enrollment.result)
            .set(ENROLLMENTS.UPDATED_AT, currentOffsetDateTime())
            .where(ENROLLMENTS.ID.eq(enrollment.id))
            .execute()
        return enrollment
    }

    private fun insert(enrollment: Enrollment): Enrollment {
        val id = dsl.insertInto(ENROLLMENTS)
            .set(ENROLLMENTS.USER_ID, userKey(enrollment.user.id))
            .set(ENROLLMENTS.SUBJECT_OFFERING_ID, enrollment.subjectOffering.id)
            .set(ENROLLMENTS.FINAL_GRADE, BigDecimal.valueOf(enrollment.finalGrade.value))
            .set(ENROLLMENTS.ABSENCES, enrollment.absences)
            .set(ENROLLMENTS.ATTENDANCE_PERCENTAGE, BigDecimal.valueOf(enrollment.attendance.percentage))
            .set(ENROLLMENTS.RESULT, enrollment.result)
            .returningResult(ENROLLMENTS.ID)
            .fetchSingle { it.value1() }
        return enrollment.copy(id = id)
    }

    private fun EnrollmentsRecord.toEnrollment(user: User, subjectOffering: SubjectOffering) = Enrollment(
        id = id,
        user = user,
        subjectOffering = subjectOffering,
        finalGrade = Grade(finalGrade?.toDouble() ?: 0.0),
        absences = notNull(ENROLLMENTS.ABSENCES),
        attendance = Attendance(attendancePercentage?.toDouble() ?: 0.0),
        result = result,
    )
}
