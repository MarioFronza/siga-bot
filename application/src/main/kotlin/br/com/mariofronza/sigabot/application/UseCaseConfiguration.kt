package br.com.mariofronza.sigabot.application

import br.com.mariofronza.sigabot.usecases.academic.ListEnrolledPeriods
import br.com.mariofronza.sigabot.usecases.academic.ListPeriodEnrollments
import br.com.mariofronza.sigabot.usecases.academic.SyncAllStudents
import br.com.mariofronza.sigabot.usecases.academic.SyncStudentGrades
import br.com.mariofronza.sigabot.usecases.academic.port.AcademicSystem
import br.com.mariofronza.sigabot.usecases.academic.port.EnrollmentRepository
import br.com.mariofronza.sigabot.usecases.identity.RegisterStudent
import br.com.mariofronza.sigabot.usecases.identity.port.InstitutionRepository
import br.com.mariofronza.sigabot.usecases.identity.port.PasswordHasher
import br.com.mariofronza.sigabot.usecases.identity.port.SecretCipher
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import br.com.mariofronza.sigabot.usecases.notification.BroadcastNotification
import br.com.mariofronza.sigabot.usecases.notification.ListNotifications
import br.com.mariofronza.sigabot.usecases.notification.NotifyStudent
import br.com.mariofronza.sigabot.usecases.notification.port.NotificationRepository
import br.com.mariofronza.sigabot.usecases.notification.port.PushSender
import br.com.mariofronza.sigabot.usecases.shared.TransactionRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UseCaseConfiguration {

    @Bean
    fun registerStudent(users: UserRepository, institutions: InstitutionRepository, hasher: PasswordHasher, cipher: SecretCipher) =
        RegisterStudent(users, institutions, hasher, cipher)

    @Bean
    fun syncStudentGrades(
        users: UserRepository,
        enrollments: EnrollmentRepository,
        academicSystems: List<AcademicSystem>,
        cipher: SecretCipher,
        pushSender: PushSender,
        transaction: TransactionRunner,
    ) = SyncStudentGrades(users, enrollments, academicSystems, cipher, pushSender, transaction)

    @Bean
    fun syncAllStudents(users: UserRepository, syncStudentGrades: SyncStudentGrades) = SyncAllStudents(users, syncStudentGrades)

    @Bean
    fun listEnrolledPeriods(enrollments: EnrollmentRepository) = ListEnrolledPeriods(enrollments)

    @Bean
    fun listPeriodEnrollments(enrollments: EnrollmentRepository) = ListPeriodEnrollments(enrollments)

    @Bean
    fun listNotifications(notifications: NotificationRepository) = ListNotifications(notifications)

    @Bean
    fun notifyStudent(users: UserRepository, notifications: NotificationRepository, pushSender: PushSender) =
        NotifyStudent(users, notifications, pushSender)

    @Bean
    fun broadcastNotification(pushSender: PushSender) = BroadcastNotification(pushSender)
}
