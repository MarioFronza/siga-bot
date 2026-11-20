package br.com.mariofronza.sigabot.persistence.misc

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.SigaCredential
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.course.entity.CourseJPA
import br.com.mariofronza.sigabot.persistence.section.entity.ClassSectionJPA
import br.com.mariofronza.sigabot.persistence.term.entity.AcademicTermJPA
import br.com.mariofronza.sigabot.persistence.notification.entity.NotificationJPA
import br.com.mariofronza.sigabot.persistence.enrollment.entity.EnrollmentJPA
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectOfferingJPA
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectJPA
import br.com.mariofronza.sigabot.persistence.user.entity.RoleJPA
import br.com.mariofronza.sigabot.persistence.user.entity.SigaCredentialJPA
import br.com.mariofronza.sigabot.persistence.user.entity.UserJPA

fun Enrollment.toJPA() = EnrollmentJPA(
    id = this.id,
    finalGrade = this.finalGrade,
    courseLoad = this.courseLoad,
    absencesCount = this.absencesCount,
    attendancePercentage = this.attendancePercentage,
    result = this.result,
    user = this.user.toJPA(),
    subjectEnrolled = this.subjectEnrolled.toJPA()
)

fun User.toJPA() = UserJPA(
    id = this.id,
    fullName = this.fullName,
    isActive = this.isActive,
    username = if (fullName.isNotEmpty()) fullName.split(" ").first() else "",
    email = this.email,
    passwordHash = this.passwordHash,
    roles = this.roles.map { it.toJPA() }.toMutableList(),
    sigaCredential = this.sigaCredential.toJPA(),
    groups = this.groups.map { it.toJPA() }.toSet()
)

fun Role.toJPA() = RoleJPA(
    id = this.id,
    name = this.name
)

fun SigaCredential.toJPA() = SigaCredentialJPA(
    id = this.id,
    cpfHash = this.cpfHash,
    passwordHash = this.passwordHash
)

fun SubjectOffering.toJPA() = SubjectOfferingJPA(
    id = this.id,
    subject = this.subject.toJPA(),
    enrolledGroup = this.enrolledGroup.toJPA()
)

fun Subject.toJPA() = SubjectJPA(
    id = this.id,
    name = this.name
)

fun ClassSection.toJPA() = ClassSectionJPA(
    id = this.id,
    name = this.name,
    course = this.course.toJPA(),
    period = this.period.toJPA(),
)

fun Course.toJPA() = CourseJPA(
    id = this.id,
    name = this.name
)

fun AcademicTerm.toJPA() = AcademicTermJPA(
    id = this.id,
    year = this.year,
    term = this.term
)

fun Notification.toJPA() = NotificationJPA(
    id = this.id,
    description = this.description,
    entityId = this.entityId,
    user = user.toJPA()
)
