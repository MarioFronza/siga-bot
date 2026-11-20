package br.com.mariofronza.sigabot.persistence

import br.com.mariofronza.sigabot.entity.catalog.SubjectOffering
import br.com.mariofronza.sigabot.entity.enrollment.Attendance
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.enrollment.Grade
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.enrollment.gateway.EnrollmentRepositoryInteractor
import br.com.mariofronza.sigabot.persistence.institution.gateway.InstitutionRepositoryInteractor
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.ENROLLMENTS
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.INSTITUTIONS
import br.com.mariofronza.sigabot.persistence.notification.gateway.NotificationRepositoryInteractor
import br.com.mariofronza.sigabot.persistence.user.gateway.AcademicAccountRepositoryInteractor
import br.com.mariofronza.sigabot.persistence.user.gateway.UserRepositoryInteractor
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.jooq.test.autoconfigure.JooqTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@JooqTest
@ImportAutoConfiguration(FlywayAutoConfiguration::class)
@Import(
    UserRepositoryInteractor::class,
    AcademicAccountRepositoryInteractor::class,
    InstitutionRepositoryInteractor::class,
    NotificationRepositoryInteractor::class,
    EnrollmentRepositoryInteractor::class,
)
class RepositoryInteractorsTest : FunSpec() {

    @Autowired lateinit var users: UserRepositoryInteractor
    @Autowired lateinit var accounts: AcademicAccountRepositoryInteractor
    @Autowired lateinit var institutions: InstitutionRepositoryInteractor
    @Autowired lateinit var notifications: NotificationRepositoryInteractor
    @Autowired lateinit var enrollments: EnrollmentRepositoryInteractor
    @Autowired lateinit var dsl: DSLContext

    private val udesc = InstitutionCode.UDESC
    private val ufpr = InstitutionCode("ufpr")

    private fun seedUfpr() {
        dsl.insertInto(INSTITUTIONS)
            .set(INSTITUTIONS.CODE, ufpr.value)
            .set(INSTITUTIONS.NAME, "Universidade Federal do Paraná")
            .set(INSTITUTIONS.GRADE_SCALE_MAX, 100.toBigDecimal())
            .execute()
    }

    private fun newUser(
        digest: ByteArray = byteArrayOf(1, 2, 3),
        email: String = "student@test.com",
        institution: InstitutionCode = udesc,
    ) = users.save(User.registerStudent(Email.of(email), "hash", AcademicAccount(institution, "login", digest, "password")))

    private fun offering(
        period: AcademicPeriod = AcademicPeriod(2026, 1),
        institution: InstitutionCode = udesc,
        course: String = "Engenharia de Software",
        classSection: String = "ESO-1",
        subject: String = "Banco de Dados $period",
    ) = SubjectOffering(institution, period, course, classSection, subject, courseLoad = 72)

    private fun enroll(user: User, offering: SubjectOffering, grade: Double = 8.5): Enrollment {
        val enrollment = Enrollment(user.id, offering, Grade(grade), absences = 2, attendance = Attendance(95.0), result = "Aprovado")
        enrollments.save(enrollment)
        return enrollment
    }

    init {
        extension(SpringExtension())

        test("keeps the id the domain gave the user") {
            val saved = newUser()

            users.findById(saved.id).shouldNotBeNull().id shouldBe saved.id
        }

        test("finds the seeded institution by its code") {
            institutions.findByCode(udesc).shouldNotBeNull().gradeScaleMax shouldBe 10.0
        }

        test("loads a user with roles and academic accounts") {
            val saved = newUser(digest = byteArrayOf(4, 5, 6))

            val loaded = users.findByEmail(Email.of("student@test.com")).shouldNotBeNull()

            loaded.id shouldBe saved.id
            loaded.roles shouldBe setOf(Role.STUDENT)
            loaded.academicAccounts.single().loginDigest shouldBe byteArrayOf(4, 5, 6)
            loaded.academicAccounts.single().loginCiphertext shouldBe "login"
            loaded.academicAccounts.single().institution shouldBe udesc
        }

        test("updates an existing user") {
            val saved = newUser()
            users.save(saved.rename("Ana Souza").copy(isActive = false))

            val loaded = users.findById(saved.id).shouldNotBeNull()
            loaded.fullName shouldBe "Ana Souza"
            loaded.isActive shouldBe false
        }

        test("lists only active users") {
            val active = newUser()
            users.save(newUser(digest = byteArrayOf(7), email = "gone@test.com").copy(isActive = false))

            users.findAllActive().map { it.id } shouldBe listOf(active.id)
        }

        test("finds an academic account by its login digest within the institution") {
            newUser(digest = byteArrayOf(9, 9, 9))

            accounts.existsByLoginDigest(udesc, byteArrayOf(9, 9, 9)) shouldBe true
        }

        test("accepts the same login in another institution") {
            seedUfpr()
            newUser(digest = byteArrayOf(9, 9, 9))
            newUser(digest = byteArrayOf(9, 9, 9), email = "other@test.com", institution = ufpr)

            accounts.existsByLoginDigest(ufpr, byteArrayOf(9, 9, 9)) shouldBe true
        }

        test("keeps subjects with the same name apart per institution") {
            seedUfpr()
            val user = newUser()
            enroll(user, offering(subject = "Cálculo I"))
            enroll(user, offering(subject = "Cálculo I", institution = ufpr), grade = 70.0)

            enrollments.find(user.id, offering(subject = "Cálculo I", institution = ufpr)).shouldNotBeNull().grade shouldBe Grade(70.0)
            enrollments.find(user.id, offering(subject = "Cálculo I")).shouldNotBeNull().grade shouldBe Grade(8.5)
        }

        test("keeps class sections with the same code apart per term and course") {
            val user = newUser()
            enroll(user, offering(period = AcademicPeriod(2026, 1), subject = "Banco de Dados"), grade = 6.0)
            enroll(user, offering(period = AcademicPeriod(2026, 2), subject = "Banco de Dados"), grade = 7.0)
            enroll(user, offering(course = "Ciência da Computação", subject = "Banco de Dados"), grade = 9.0)

            enrollments.find(user.id, offering(period = AcademicPeriod(2026, 2), subject = "Banco de Dados")).shouldNotBeNull().grade shouldBe Grade(7.0)
            enrollments.find(user.id, offering(course = "Ciência da Computação", subject = "Banco de Dados")).shouldNotBeNull().grade shouldBe Grade(9.0)
        }

        test("stores the notification message") {
            val user = newUser()
            notifications.save(Notification(recipient = user.id, message = "Nova nota em Banco de Dados"))

            notifications.findByRecipient(user.id, PageRequest.of(0, 20)).content.single().message shouldBe "Nova nota em Banco de Dados"
        }

        test("pages a user's notifications newest first, ignoring the requested sort") {
            val user = newUser()
            listOf("primeira", "segunda", "terceira").forEach { notifications.save(Notification(recipient = user.id, message = it)) }

            val page = notifications.findByRecipient(user.id, PageRequest.of(0, 2, Sort.by("unknown")))

            page.content.map { it.message } shouldBe listOf("terceira", "segunda")
            page.totalElements shouldBe 3
        }

        test("lists the academic periods a user is enrolled in, newest first") {
            val user = newUser()
            enroll(user, offering(period = AcademicPeriod(2026, 1)))
            enroll(user, offering(period = AcademicPeriod(2026, 2)))

            enrollments.findPeriods(user.id) shouldBe listOf(AcademicPeriod(2026, 2), AcademicPeriod(2026, 1))
        }

        test("updates the grade of an existing enrollment instead of adding a row") {
            val user = newUser()
            val enrollment = enroll(user, offering())
            enrollments.save(enrollment.copy(grade = Grade(9.75)))

            enrollments.find(user.id, offering()).shouldNotBeNull().grade shouldBe Grade(9.75)
            dsl.fetchCount(ENROLLMENTS) shouldBe 1
        }

        test("lists the enrollments of a term with their offering") {
            val user = newUser()
            enroll(user, offering(period = AcademicPeriod(2026, 1)))
            enroll(user, offering(period = AcademicPeriod(2026, 2)))

            val enrollment = enrollments.findAll(user.id, AcademicPeriod(2026, 1)).single()
            enrollment.offering shouldBe offering(period = AcademicPeriod(2026, 1))
            enrollment.grade shouldBe Grade(8.5)
            enrollment.attendance shouldBe Attendance(95.0)
        }
    }
}
