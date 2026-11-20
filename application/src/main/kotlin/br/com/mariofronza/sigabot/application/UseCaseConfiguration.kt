package br.com.mariofronza.sigabot.application

import br.com.mariofronza.sigabot.usecases.academic.ListEnrolledPeriods
import br.com.mariofronza.sigabot.usecases.academic.ListPeriodEnrollments
import br.com.mariofronza.sigabot.usecases.academic.RunDueSyncs
import br.com.mariofronza.sigabot.usecases.academic.SyncStudentGrades
import br.com.mariofronza.sigabot.usecases.academic.port.AcademicSystem
import br.com.mariofronza.sigabot.usecases.academic.port.EnrollmentRepository
import br.com.mariofronza.sigabot.usecases.academic.port.SyncObserver
import br.com.mariofronza.sigabot.usecases.academic.port.SyncQueue
import br.com.mariofronza.sigabot.usecases.identity.RegisterStudent
import br.com.mariofronza.sigabot.usecases.identity.port.InstitutionRepository
import br.com.mariofronza.sigabot.usecases.identity.port.PasswordHasher
import br.com.mariofronza.sigabot.usecases.identity.port.SecretCipher
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import br.com.mariofronza.sigabot.usecases.notification.BroadcastNotification
import br.com.mariofronza.sigabot.usecases.notification.DeliverOutbox
import br.com.mariofronza.sigabot.usecases.notification.ListNotifications
import br.com.mariofronza.sigabot.usecases.notification.NotifyStudent
import br.com.mariofronza.sigabot.usecases.notification.port.NotificationRepository
import br.com.mariofronza.sigabot.usecases.notification.port.Outbox
import br.com.mariofronza.sigabot.usecases.notification.port.PushSender
import br.com.mariofronza.sigabot.usecases.shared.TransactionRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class UseCaseConfiguration {

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun registerStudent(
        users: UserRepository,
        institutions: InstitutionRepository,
        hasher: PasswordHasher,
        cipher: SecretCipher,
        queue: SyncQueue,
        transaction: TransactionRunner,
        clock: Clock,
    ) = RegisterStudent(users, institutions, hasher, cipher, queue, transaction, clock)

    @Bean
    fun syncStudentGrades(
        users: UserRepository,
        enrollments: EnrollmentRepository,
        academicSystems: List<AcademicSystem>,
        cipher: SecretCipher,
        outbox: Outbox,
        transaction: TransactionRunner,
    ) = SyncStudentGrades(users, enrollments, academicSystems, cipher, outbox, transaction)

    @Bean
    fun runDueSyncs(queue: SyncQueue, users: UserRepository, syncStudentGrades: SyncStudentGrades, observer: SyncObserver, clock: Clock) =
        RunDueSyncs(queue, users, syncStudentGrades, observer, clock)

    @Bean
    fun deliverOutbox(outbox: Outbox, pushSender: PushSender) = DeliverOutbox(outbox, pushSender)

    @Bean
    fun listEnrolledPeriods(enrollments: EnrollmentRepository) = ListEnrolledPeriods(enrollments)

    @Bean
    fun listPeriodEnrollments(enrollments: EnrollmentRepository) = ListPeriodEnrollments(enrollments)

    @Bean
    fun listNotifications(notifications: NotificationRepository) = ListNotifications(notifications)

    @Bean
    fun notifyStudent(users: UserRepository, notifications: NotificationRepository, outbox: Outbox, transaction: TransactionRunner) =
        NotifyStudent(users, notifications, outbox, transaction)

    @Bean
    fun broadcastNotification(pushSender: PushSender) = BroadcastNotification(pushSender)
}
