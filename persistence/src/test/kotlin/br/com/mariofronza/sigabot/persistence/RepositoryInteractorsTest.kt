package br.com.mariofronza.sigabot.persistence

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.course.gateway.CourseRepositoryInteractor
import br.com.mariofronza.sigabot.persistence.enrollment.gateway.EnrollmentRepositoryInteractor
import br.com.mariofronza.sigabot.persistence.notification.gateway.NotificationRepositoryInteractor
import br.com.mariofronza.sigabot.persistence.section.gateway.ClassSectionRepositoryInteractor
import br.com.mariofronza.sigabot.persistence.subject.gateway.SubjectOfferingRepositoryInteractor
import br.com.mariofronza.sigabot.persistence.subject.gateway.SubjectRepositoryInteractor
import br.com.mariofronza.sigabot.persistence.term.gateway.AcademicTermRepositoryInteractor
import br.com.mariofronza.sigabot.persistence.user.gateway.RoleRepositoryInteractor
import br.com.mariofronza.sigabot.persistence.institution.gateway.InstitutionRepositoryInteractor
import br.com.mariofronza.sigabot.persistence.user.gateway.AcademicAccountRepositoryInteractor
import br.com.mariofronza.sigabot.persistence.user.gateway.UserRepositoryInteractor
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import br.com.mariofronza.sigabot.persistence.jooq.tables.references.INSTITUTIONS
import org.jooq.DSLContext
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
    RoleRepositoryInteractor::class,
    AcademicAccountRepositoryInteractor::class,
    InstitutionRepositoryInteractor::class,
    NotificationRepositoryInteractor::class,
    CourseRepositoryInteractor::class,
    AcademicTermRepositoryInteractor::class,
    ClassSectionRepositoryInteractor::class,
    SubjectRepositoryInteractor::class,
    SubjectOfferingRepositoryInteractor::class,
    EnrollmentRepositoryInteractor::class,
)
class RepositoryInteractorsTest : FunSpec() {

    @Autowired lateinit var users: UserRepositoryInteractor
    @Autowired lateinit var roles: RoleRepositoryInteractor
    @Autowired lateinit var accounts: AcademicAccountRepositoryInteractor
    @Autowired lateinit var institutions: InstitutionRepositoryInteractor
    @Autowired lateinit var dsl: DSLContext
    @Autowired lateinit var notifications: NotificationRepositoryInteractor
    @Autowired lateinit var courses: CourseRepositoryInteractor
    @Autowired lateinit var academicTerms: AcademicTermRepositoryInteractor
    @Autowired lateinit var classSections: ClassSectionRepositoryInteractor
    @Autowired lateinit var subjects: SubjectRepositoryInteractor
    @Autowired lateinit var subjectOfferings: SubjectOfferingRepositoryInteractor
    @Autowired lateinit var enrollments: EnrollmentRepositoryInteractor

    private fun udesc() = institutions.findByCode(Institution.UDESC).shouldNotBeNull()

    private fun ufpr(): Institution {
        dsl.insertInto(INSTITUTIONS)
            .set(INSTITUTIONS.CODE, "ufpr")
            .set(INSTITUTIONS.NAME, "Universidade Federal do Paraná")
            .set(INSTITUTIONS.GRADE_SCALE_MAX, 100.toBigDecimal())
            .execute()
        return institutions.findByCode("ufpr").shouldNotBeNull()
    }

    private fun newUser(
        digest: ByteArray = byteArrayOf(1, 2, 3),
        email: String = "student@test.com",
        institution: Institution = udesc(),
    ) = users.save(
        User(
            isActive = true,
            email = email,
            passwordHash = "hash",
            roles = listOf(roles.findByName("STUDENT").shouldNotBeNull()),
            academicAccounts = listOf(
                AcademicAccount(
                    institution = institution,
                    loginCiphertext = "login",
                    loginDigest = digest,
                    passwordCiphertext = "password",
                )
            ),
        )
    )

    private fun enroll(user: User, year: Int, term: Int): Enrollment {
        val course = courses.save(Course(institution = udesc(), name = "Engenharia de Software $year/$term"))
        val academicTerm = academicTerms.save(AcademicTerm(institution = udesc(), year = year, term = term))
        val classSection = classSections.save(ClassSection(code = "ESO-1", course = course, academicTerm = academicTerm))
        val subject = subjects.save(Subject(institution = udesc(), name = "Banco de Dados $year/$term"))
        val offering = subjectOfferings.save(SubjectOffering(subject = subject, classSection = classSection, courseLoad = 72))
        return enrollments.save(
            Enrollment(
                user = user,
                subjectOffering = offering,
                finalGrade = 8.5,
                absences = 2,
                attendancePercentage = 95.0,
                result = "Aprovado",
            )
        )
    }

    init {
        extension(SpringExtension())

        test("generates a time-ordered external id for a new user") {
            newUser().externalId.shouldNotBeNull().version() shouldBe 7
        }

        test("loads a user with roles and academic accounts") {
            val saved = newUser(digest = byteArrayOf(4, 5, 6))

            val loaded = users.findByEmail("student@test.com").shouldNotBeNull()

            loaded.externalId shouldBe saved.externalId
            loaded.roles.map { it.name } shouldBe listOf("STUDENT")
            loaded.academicAccounts.single().loginDigest shouldBe byteArrayOf(4, 5, 6)
            loaded.academicAccounts.single().loginCiphertext shouldBe "login"
            loaded.academicAccounts.single().institution.code shouldBe Institution.UDESC
        }

        test("updates an existing user") {
            val saved = newUser()
            users.save(saved.copy(fullName = "Ana Souza", isActive = false))

            val loaded = users.findByExternalId(saved.externalId.shouldNotBeNull()).shouldNotBeNull()
            loaded.fullName shouldBe "Ana Souza"
            loaded.isActive shouldBe false
        }

        test("lists only active users") {
            val active = newUser()
            users.save(newUser(digest = byteArrayOf(7), email = "gone@test.com").copy(isActive = false))

            users.findAllActive().map { it.externalId } shouldBe listOf(active.externalId)
        }

        test("finds the catalog rows of a term") {
            val user = newUser()
            val offering = enroll(user, 2026, 1).subjectOffering
            val section = offering.classSection

            courses.findByName(udesc(), section.course.name).shouldNotBeNull().id shouldBe section.course.id
            academicTerms.findByYearAndTerm(udesc(), 2026, 1).shouldNotBeNull().id shouldBe section.academicTerm.id
            classSections.find("ESO-1", section.course, section.academicTerm).shouldNotBeNull().id shouldBe section.id
            subjectOfferings.find(offering.subject, section).shouldNotBeNull().courseLoad shouldBe 72
        }

        test("lists the enrollments of a term with their offering") {
            val user = newUser()
            enroll(user, 2026, 1)
            enroll(user, 2026, 2)

            val enrollment = enrollments.findAll(user, 2026, 1).single()
            enrollment.subjectOffering.subject.name shouldBe "Banco de Dados 2026/1"
            enrollment.subjectOffering.classSection.code shouldBe "ESO-1"
            enrollment.subjectOffering.classSection.academicTerm.term shouldBe 1
            enrollment.finalGrade shouldBe 8.5
            enrollment.attendancePercentage shouldBe 95.0
        }

        test("finds an academic account by its login digest within the institution") {
            newUser(digest = byteArrayOf(9, 9, 9))

            accounts.existsByLoginDigest(udesc(), byteArrayOf(9, 9, 9)) shouldBe true
        }

        test("accepts the same login in another institution") {
            val ufpr = ufpr()
            newUser(digest = byteArrayOf(9, 9, 9))
            newUser(digest = byteArrayOf(9, 9, 9), email = "other@test.com", institution = ufpr)

            accounts.existsByLoginDigest(ufpr, byteArrayOf(9, 9, 9)) shouldBe true
        }

        test("keeps subjects with the same name apart per institution") {
            subjects.save(Subject(institution = udesc(), name = "Cálculo I"))
            val atUfpr = subjects.save(Subject(institution = ufpr(), name = "Cálculo I"))

            subjects.findByName(atUfpr.institution, "Cálculo I").shouldNotBeNull().id shouldBe atUfpr.id
        }

        test("stores the notification message") {
            val user = newUser()
            notifications.save(Notification(user = user, message = "Nova nota em Banco de Dados"))

            notifications.findByUser(user, PageRequest.of(0, 20)).content.single().message shouldBe "Nova nota em Banco de Dados"
        }

        test("pages a user's notifications newest first, ignoring the requested sort") {
            val user = newUser()
            listOf("primeira", "segunda", "terceira").forEach { notifications.save(Notification(user = user, message = it)) }

            val page = notifications.findByUser(user, PageRequest.of(0, 2, Sort.by("unknown")))

            page.content.map { it.message } shouldBe listOf("terceira", "segunda")
            page.totalElements shouldBe 3
        }

        test("lists the academic terms a user is enrolled in, newest first") {
            val user = newUser()
            enroll(user, 2026, 1)
            enroll(user, 2026, 2)

            academicTerms.findAllEnrolledBy(user).map { it.year to it.term } shouldBe listOf(2026 to 2, 2026 to 1)
        }

        test("updates the grade of an existing enrollment") {
            val user = newUser()
            val enrollment = enroll(user, 2026, 1)
            enrollments.save(enrollment.copy(finalGrade = 9.75))

            enrollments.find(user, enrollment.subjectOffering).shouldNotBeNull().finalGrade shouldBe 9.75
        }
    }
}
