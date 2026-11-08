package br.com.mariofronza.sigabot.persistence.misc

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.group.Group
import br.com.mariofronza.sigabot.entity.group.Period
import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.semester.SemesterResult
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.entity.subject.SubjectEnrollment
import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.SigaCredential
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.course.entity.CourseJPA
import br.com.mariofronza.sigabot.persistence.group.entity.GroupJPA
import br.com.mariofronza.sigabot.persistence.group.entity.PeriodJPA
import br.com.mariofronza.sigabot.persistence.notification.entity.NotificationJPA
import br.com.mariofronza.sigabot.persistence.semester.entity.SemesterResultJPA
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectEnrollmentJPA
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectJPA
import br.com.mariofronza.sigabot.persistence.user.entity.RoleJPA
import br.com.mariofronza.sigabot.persistence.user.entity.SigaCredentialJPA
import br.com.mariofronza.sigabot.persistence.user.entity.UserJPA

fun SemesterResult.toJPA() = SemesterResultJPA(
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

fun SubjectEnrollment.toJPA() = SubjectEnrollmentJPA(
    id = this.id,
    subject = this.subject.toJPA(),
    enrolledGroup = this.enrolledGroup.toJPA()
)

fun Subject.toJPA() = SubjectJPA(
    id = this.id,
    name = this.name
)

fun Group.toJPA() = GroupJPA(
    id = this.id,
    name = this.name,
    course = this.course.toJPA(),
    period = this.period.toJPA(),
)

fun Course.toJPA() = CourseJPA(
    id = this.id,
    name = this.name
)

fun Period.toJPA() = PeriodJPA(
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
