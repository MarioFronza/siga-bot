package br.com.mariofronza.sigabot.application

import org.springframework.beans.factory.annotation.Value
import br.com.mariofronza.sigabot.usecases.identity.port.RefreshTokenRepository
import br.com.mariofronza.sigabot.usecases.identity.port.AccessTokenIssuer
import br.com.mariofronza.sigabot.usecases.identity.RevokeSession
import br.com.mariofronza.sigabot.usecases.identity.RefreshSession
import br.com.mariofronza.sigabot.usecases.identity.OpenSession
import br.com.mariofronza.sigabot.usecases.identity.GrantAdmins
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.usecases.academic.ListEnrolledPeriods
import br.com.mariofronza.sigabot.usecases.academic.ListPeriodEnrollments
import br.com.mariofronza.sigabot.usecases.academic.RunDueSyncs
import br.com.mariofronza.sigabot.usecases.academic.RequestSync
import br.com.mariofronza.sigabot.usecases.academic.ShowSyncStatus
import br.com.mariofronza.sigabot.usecases.academic.SyncStudentGrades
import br.com.mariofronza.sigabot.usecases.academic.port.AcademicSystem
import br.com.mariofronza.sigabot.usecases.academic.port.EnrollmentRepository
import br.com.mariofronza.sigabot.usecases.academic.port.SyncObserver
import br.com.mariofronza.sigabot.usecases.academic.port.SyncQueue
import br.com.mariofronza.sigabot.usecases.identity.RegisterStudent
import br.com.mariofronza.sigabot.usecases.identity.SearchUsers
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
    fun adminEmails(@Value("\${admin.emails:}") raw: String): Set<Email> =
        raw.split(",").map(String::trim).filter(String::isNotEmpty).map(Email::of).toSet()

    @Bean
    fun grantAdmins(users: UserRepository, adminEmails: Set<Email>) = GrantAdmins(users, adminEmails)

    @Bean
    fun openSession(tokens: RefreshTokenRepository, accessTokens: AccessTokenIssuer, clock: Clock) =
        OpenSession(tokens, accessTokens, clock)

    @Bean
    fun refreshSession(tokens: RefreshTokenRepository, users: UserRepository, openSession: OpenSession, transaction: TransactionRunner, clock: Clock) =
        RefreshSession(tokens, users, openSession, transaction, clock)

    @Bean
    fun revokeSession(tokens: RefreshTokenRepository, clock: Clock) = RevokeSession(tokens, clock)

    @Bean
    fun registerStudent(
        users: UserRepository,
        institutions: InstitutionRepository,
        hasher: PasswordHasher,
        cipher: SecretCipher,
        queue: SyncQueue,
        transaction: TransactionRunner,
        clock: Clock,
        adminEmails: Set<Email>,
    ) = RegisterStudent(users, institutions, hasher, cipher, queue, transaction, clock, adminEmails)

    @Bean
    fun syncStudentGrades(
        users: UserRepository,
        enrollments: EnrollmentRepository,
        academicSystems: List<AcademicSystem>,
        cipher: SecretCipher,
        outbox: Outbox,
        notifications: NotificationRepository,
        transaction: TransactionRunner,
    ) = SyncStudentGrades(users, enrollments, academicSystems, cipher, outbox, notifications, transaction)

    @Bean
    fun runDueSyncs(queue: SyncQueue, users: UserRepository, syncStudentGrades: SyncStudentGrades, observer: SyncObserver, clock: Clock) =
        RunDueSyncs(queue, users, syncStudentGrades, observer, clock)

    @Bean
    fun deliverOutbox(outbox: Outbox, pushSender: PushSender, clock: Clock) = DeliverOutbox(outbox, pushSender, clock)

    @Bean
    fun searchUsers(users: UserRepository) = SearchUsers(users)

    @Bean
    fun requestSync(queue: SyncQueue, clock: Clock) = RequestSync(queue, clock)

    @Bean
    fun showSyncStatus(queue: SyncQueue, clock: Clock) = ShowSyncStatus(queue, clock)

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
    fun broadcastNotification(notifications: NotificationRepository, pushSender: PushSender) = BroadcastNotification(notifications, pushSender)
}
