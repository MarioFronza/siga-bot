package br.com.mariofronza.sigabot.persistence.enrollment

import org.jooq.Table
import org.jooq.TableField
import org.jooq.impl.DSL.row
import br.com.mariofronza.sigabot.entity.catalog.SubjectOffering
import br.com.mariofronza.sigabot.entity.enrollment.Attendance
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.enrollment.Grade
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.persistence.institution.institutionKey
import br.com.mariofronza.sigabot.persistence.jooq.tables.records.EnrollmentsRecord
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.ACADEMIC_TERMS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.CLASS_SECTIONS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.COURSES
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.ENROLLMENTS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.INSTITUTIONS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.SUBJECTS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.SUBJECT_OFFERINGS
import br.com.mariofronza.sigabot.persistence.notNull
import br.com.mariofronza.sigabot.persistence.user.userKey
import br.com.mariofronza.sigabot.usecases.academic.port.EnrollmentRepository
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectJoinStep
import org.jooq.impl.DSL.and
import org.jooq.impl.DSL.currentOffsetDateTime
import org.jooq.impl.DSL.excluded
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
@Transactional
class JooqEnrollmentRepository(
    private val dsl: DSLContext,
) : EnrollmentRepository {

    override fun find(studentId: UserId, offering: SubjectOffering): Enrollment? =
        dsl.select(ENROLLMENTS)
            .from(ENROLLMENTS)
            .joinCatalog()
            .where(ENROLLMENTS.USER_ID.eq(userKey(studentId)).and(identifies(offering)))
            .fetchOne { (enrollment) -> enrollment.toEnrollment(studentId, offering) }

    override fun findAll(studentId: UserId, period: AcademicPeriod): List<Enrollment> =
        dsl.select(ENROLLMENTS, SUBJECT_OFFERINGS, SUBJECTS, CLASS_SECTIONS, COURSES, INSTITUTIONS)
            .from(ENROLLMENTS)
            .joinCatalog()
            .where(
                ENROLLMENTS.USER_ID.eq(userKey(studentId)),
                ACADEMIC_TERMS.YEAR.eq(period.year),
                ACADEMIC_TERMS.TERM.eq(period.term),
            )
            .orderBy(ENROLLMENTS.ID)
            .fetch { (enrollment, offering, subject, section, course, institution) ->
                enrollment.toEnrollment(
                    studentId,
                    SubjectOffering(
                        institution = InstitutionCode(institution.code),
                        period = period,
                        course = course.name,
                        classSection = section.code,
                        subject = subject.name,
                        courseLoad = offering.courseLoad,
                    ),
                )
            }

    override fun findPeriods(studentId: UserId): List<AcademicPeriod> =
        dsl.selectDistinct(ACADEMIC_TERMS.YEAR, ACADEMIC_TERMS.TERM)
            .from(ENROLLMENTS)
            .joinCatalog()
            .where(ENROLLMENTS.USER_ID.eq(userKey(studentId)))
            .orderBy(ACADEMIC_TERMS.YEAR.desc(), ACADEMIC_TERMS.TERM.desc())
            .fetch { AcademicPeriod(it.notNull(ACADEMIC_TERMS.YEAR), it.notNull(ACADEMIC_TERMS.TERM)) }

    override fun save(enrollment: Enrollment) {
        val offeringId = saveOffering(enrollment.offering)
        dsl.insertInto(ENROLLMENTS)
            .set(ENROLLMENTS.USER_ID, userKey(enrollment.studentId))
            .set(ENROLLMENTS.SUBJECT_OFFERING_ID, offeringId)
            .set(ENROLLMENTS.FINAL_GRADE, BigDecimal.valueOf(enrollment.grade.value))
            .set(ENROLLMENTS.ABSENCES, enrollment.absences)
            .set(ENROLLMENTS.ATTENDANCE_PERCENTAGE, BigDecimal.valueOf(enrollment.attendance.percentage))
            .set(ENROLLMENTS.RESULT, enrollment.result)
            .onConflict(ENROLLMENTS.USER_ID, ENROLLMENTS.SUBJECT_OFFERING_ID)
            .doUpdate()
            .set(ENROLLMENTS.FINAL_GRADE, excluded(ENROLLMENTS.FINAL_GRADE))
            .set(ENROLLMENTS.ABSENCES, excluded(ENROLLMENTS.ABSENCES))
            .set(ENROLLMENTS.ATTENDANCE_PERCENTAGE, excluded(ENROLLMENTS.ATTENDANCE_PERCENTAGE))
            .set(ENROLLMENTS.RESULT, excluded(ENROLLMENTS.RESULT))
            .set(ENROLLMENTS.UPDATED_AT, currentOffsetDateTime())
            .where(
                row(ENROLLMENTS.FINAL_GRADE, ENROLLMENTS.ABSENCES, ENROLLMENTS.ATTENDANCE_PERCENTAGE, ENROLLMENTS.RESULT).isDistinctFrom(
                    excluded(ENROLLMENTS.FINAL_GRADE),
                    excluded(ENROLLMENTS.ABSENCES),
                    excluded(ENROLLMENTS.ATTENDANCE_PERCENTAGE),
                    excluded(ENROLLMENTS.RESULT),
                )
            )
            .execute()
    }

    private fun saveOffering(offering: SubjectOffering): Long {
        val institutionId = institutionKey(offering.institution)
        val courseId = idOf(COURSES, COURSES.ID, COURSES.INSTITUTION_ID.eq(institutionId).and(COURSES.NAME.eq(offering.course))) {
            dsl.insertInto(COURSES).set(COURSES.INSTITUTION_ID, institutionId).set(COURSES.NAME, offering.course)
                .onConflictDoNothing().returning(COURSES.ID).fetchOne(COURSES.ID)
        }
        val termKey = ACADEMIC_TERMS.INSTITUTION_ID.eq(institutionId)
            .and(ACADEMIC_TERMS.YEAR.eq(offering.period.year)).and(ACADEMIC_TERMS.TERM.eq(offering.period.term))
        val termId = idOf(ACADEMIC_TERMS, ACADEMIC_TERMS.ID, termKey) {
            dsl.insertInto(ACADEMIC_TERMS).set(ACADEMIC_TERMS.INSTITUTION_ID, institutionId)
                .set(ACADEMIC_TERMS.YEAR, offering.period.year).set(ACADEMIC_TERMS.TERM, offering.period.term)
                .onConflictDoNothing().returning(ACADEMIC_TERMS.ID).fetchOne(ACADEMIC_TERMS.ID)
        }
        val subjectId = idOf(SUBJECTS, SUBJECTS.ID, SUBJECTS.INSTITUTION_ID.eq(institutionId).and(SUBJECTS.NAME.eq(offering.subject))) {
            dsl.insertInto(SUBJECTS).set(SUBJECTS.INSTITUTION_ID, institutionId).set(SUBJECTS.NAME, offering.subject)
                .onConflictDoNothing().returning(SUBJECTS.ID).fetchOne(SUBJECTS.ID)
        }
        val sectionKey = CLASS_SECTIONS.ACADEMIC_TERM_ID.eq(termId)
            .and(CLASS_SECTIONS.COURSE_ID.eq(courseId)).and(CLASS_SECTIONS.CODE.eq(offering.classSection))
        val sectionId = idOf(CLASS_SECTIONS, CLASS_SECTIONS.ID, sectionKey) {
            dsl.insertInto(CLASS_SECTIONS).set(CLASS_SECTIONS.ACADEMIC_TERM_ID, termId)
                .set(CLASS_SECTIONS.COURSE_ID, courseId).set(CLASS_SECTIONS.CODE, offering.classSection)
                .onConflictDoNothing().returning(CLASS_SECTIONS.ID).fetchOne(CLASS_SECTIONS.ID)
        }
        val offeringKey = SUBJECT_OFFERINGS.CLASS_SECTION_ID.eq(sectionId).and(SUBJECT_OFFERINGS.SUBJECT_ID.eq(subjectId))
        return idOf(SUBJECT_OFFERINGS, SUBJECT_OFFERINGS.ID, offeringKey) {
            dsl.insertInto(SUBJECT_OFFERINGS)
                .set(SUBJECT_OFFERINGS.CLASS_SECTION_ID, sectionId)
                .set(SUBJECT_OFFERINGS.SUBJECT_ID, subjectId)
                .set(SUBJECT_OFFERINGS.COURSE_LOAD, offering.courseLoad)
                .onConflict(SUBJECT_OFFERINGS.CLASS_SECTION_ID, SUBJECT_OFFERINGS.SUBJECT_ID)
                .doUpdate().set(SUBJECT_OFFERINGS.COURSE_LOAD, excluded(SUBJECT_OFFERINGS.COURSE_LOAD))
                .where(SUBJECT_OFFERINGS.COURSE_LOAD.isDistinctFrom(excluded(SUBJECT_OFFERINGS.COURSE_LOAD)))
                .returning(SUBJECT_OFFERINGS.ID).fetchOne(SUBJECT_OFFERINGS.ID)
        }
    }

    private fun <R : Record> idOf(table: Table<R>, id: TableField<R, Long?>, key: Condition, write: () -> Long?): Long {
        val find = { dsl.select(id).from(table).where(key).fetchOne(id) }
        return write() ?: checkNotNull(find()) { "No ${table.name} row matches $key after writing it" }
    }

    private fun <R : Record> SelectJoinStep<R>.joinCatalog() =
        join(SUBJECT_OFFERINGS).on(SUBJECT_OFFERINGS.ID.eq(ENROLLMENTS.SUBJECT_OFFERING_ID))
            .join(SUBJECTS).on(SUBJECTS.ID.eq(SUBJECT_OFFERINGS.SUBJECT_ID))
            .join(CLASS_SECTIONS).on(CLASS_SECTIONS.ID.eq(SUBJECT_OFFERINGS.CLASS_SECTION_ID))
            .join(COURSES).on(COURSES.ID.eq(CLASS_SECTIONS.COURSE_ID))
            .join(ACADEMIC_TERMS).on(ACADEMIC_TERMS.ID.eq(CLASS_SECTIONS.ACADEMIC_TERM_ID))
            .join(INSTITUTIONS).on(INSTITUTIONS.ID.eq(ACADEMIC_TERMS.INSTITUTION_ID))

    private fun identifies(offering: SubjectOffering): Condition = and(
        INSTITUTIONS.CODE.eq(offering.institution.value),
        ACADEMIC_TERMS.YEAR.eq(offering.period.year),
        ACADEMIC_TERMS.TERM.eq(offering.period.term),
        COURSES.NAME.eq(offering.course),
        CLASS_SECTIONS.CODE.eq(offering.classSection),
        SUBJECTS.NAME.eq(offering.subject),
    )

    private fun EnrollmentsRecord.toEnrollment(studentId: UserId, offering: SubjectOffering) = Enrollment(
        studentId = studentId,
        offering = offering,
        grade = Grade(finalGrade?.toDouble() ?: 0.0),
        absences = notNull(ENROLLMENTS.ABSENCES),
        attendance = Attendance(attendancePercentage?.toDouble() ?: 0.0),
        result = result,
    )
}
