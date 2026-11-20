package br.com.mariofronza.sigabot.http

import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.enrollment.Attendance
import br.com.mariofronza.sigabot.entity.enrollment.Grade
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.http.security.AuthService
import br.com.mariofronza.sigabot.http.security.SecurityConfig
import br.com.mariofronza.sigabot.http.security.UserDetailsImpl
import br.com.mariofronza.sigabot.http.security.JWTUtil
import br.com.mariofronza.sigabot.usecases.notification.NotificationService
import br.com.mariofronza.sigabot.usecases.notification.dto.NotificationOutput
import br.com.mariofronza.sigabot.usecases.retrieve.RetrieveService
import br.com.mariofronza.sigabot.usecases.siga.enrollment.EnrollmentService
import br.com.mariofronza.sigabot.usecases.siga.term.AcademicTermService
import br.com.mariofronza.sigabot.usecases.siga.term.dto.AcademicTermOutput
import br.com.mariofronza.sigabot.usecases.user.EmailAlreadyRegisteredException
import br.com.mariofronza.sigabot.usecases.user.UserNotFoundException
import br.com.mariofronza.sigabot.usecases.user.UserService
import br.com.mariofronza.sigabot.usecases.user.dto.UserOutput
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.nulls.shouldNotBeNull
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.util.UUID

@WebMvcTest
@Import(SecurityConfig::class, JWTUtil::class, AuthService::class, ApiTest.Mocks::class)
@TestPropertySource(properties = ["jwt.secret=test-secret-test-secret-test-secret-0123"])
class ApiTest : FunSpec() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jwtUtil: JWTUtil
    @Autowired lateinit var userDetailsService: UserDetailsService
    @Autowired lateinit var userService: UserService
    @Autowired lateinit var retrieveService: RetrieveService
    @Autowired lateinit var academicTermService: AcademicTermService
    @Autowired lateinit var enrollmentService: EnrollmentService
    @Autowired lateinit var notificationService: NotificationService

    @TestConfiguration
    class Mocks {
        @Bean fun userDetailsService(): UserDetailsService = mockk()
        @Bean fun userService(): UserService = mockk()
        @Bean fun retrieveService(): RetrieveService = mockk()
        @Bean fun academicTermService(): AcademicTermService = mockk()
        @Bean fun enrollmentService(): EnrollmentService = mockk()
        @Bean fun notificationService(): NotificationService = mockk(relaxed = true)
    }

    private val externalId = UUID.fromString("01a1f973-13ad-7a5c-80df-bf421ada5df1")
    private val passwordHash = BCryptPasswordEncoder().encode("app-password").shouldNotBeNull()
    private val udesc = Institution(id = 1, code = Institution.UDESC, name = "UDESC", gradeScaleMax = 10.0)

    private fun signedIn(vararg roles: String): User {
        val user = User(
            id = UserId(externalId),
            isActive = true,
            fullName = "Ana Souza",
            email = Email.of("ana@test.com"),
            passwordHash = passwordHash,
            roles = roles.map { Role(name = it) },
            academicAccounts = emptyList(),
        )
        every { userDetailsService.loadUserByUsername("ana@test.com") } returns UserDetailsImpl(user)
        return user
    }

    private fun MockHttpServletRequestDsl.bearer(user: User) {
        header("Authorization", "Bearer ${jwtUtil.generateAccessToken(UserDetailsImpl(user), "test")}")
    }

    private fun MockHttpServletRequestDsl.json(body: String) {
        contentType = APPLICATION_JSON
        content = body
    }

    private fun enrollment(user: User): Enrollment {
        val term = AcademicTerm(institution = udesc, period = AcademicPeriod(2026, 1))
        val section = ClassSection(code = "ESO-1", course = Course(institution = udesc, name = "ESO"), academicTerm = term)
        return Enrollment(
            user = user,
            subjectOffering = SubjectOffering(subject = Subject(institution = udesc, name = "Banco de Dados"), classSection = section, courseLoad = 72),
            finalGrade = Grade(8.5),
            absences = 2,
            attendance = Attendance(95.0),
            result = "Aprovado",
        )
    }

    init {
        extension(SpringExtension())

        test("keeps the health check outside the versioned api") {
            mockMvc.get("/health").andExpect { status { isOk() } }
        }

        test("signs a student up and points at the profile") {
            every { userService.createUser(any()) } returns UserOutput(externalId, "ana@test.com", "")

            mockMvc.post("/api/v1/users") {
                json("""{"email":"ana@test.com","password":"app-password","sigaCredential":{"cpf":"000","password":"siga"}}""")
            }.andExpect {
                status { isCreated() }
                header { string("Location", "http://localhost/api/v1/me") }
                jsonPath("$.id") { value(externalId.toString()) }
            }
        }

        test("rejects an invalid sign up as a problem") {
            mockMvc.post("/api/v1/users") {
                json("""{"email":"not-an-email","password":"","sigaCredential":{"cpf":"000","password":"siga"}}""")
            }.andExpect {
                status { isBadRequest() }
                content { contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON) }
            }
        }

        test("answers 409 when the email is already registered") {
            every { userService.createUser(any()) } throws EmailAlreadyRegisteredException()

            mockMvc.post("/api/v1/users") {
                json("""{"email":"ana@test.com","password":"app-password","sigaCredential":{"cpf":"000","password":"siga"}}""")
            }.andExpect {
                status { isConflict() }
                content { contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON) }
            }
        }

        test("opens a session with the app credentials") {
            signedIn("STUDENT")

            mockMvc.post("/api/v1/sessions") { json("""{"email":"ana@test.com","password":"app-password"}""") }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.accessToken") { isString() }
                    jsonPath("$.userId") { value(externalId.toString()) }
                }
        }

        test("refuses a session with a wrong password") {
            signedIn("STUDENT")

            mockMvc.post("/api/v1/sessions") { json("""{"email":"ana@test.com","password":"wrong"}""") }
                .andExpect { status { isUnauthorized() } }
        }

        test("refreshes a session from the refresh token in the body") {
            val user = signedIn("STUDENT")
            val refreshToken = jwtUtil.generateRefreshToken(UserDetailsImpl(user), "test")

            mockMvc.post("/api/v1/sessions/refresh") { json("""{"refreshToken":"$refreshToken"}""") }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.accessToken") { isString() }
                }
        }

        test("answers 401 as a problem when the refresh token is rejected") {
            mockMvc.post("/api/v1/sessions/refresh") { json("""{"refreshToken":"invalid"}""") }
                .andExpect {
                    status { isUnauthorized() }
                    content { contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON) }
                }
        }

        test("answers 401 to a request without a token") {
            mockMvc.get("/api/v1/me").andExpect { status { isUnauthorized() } }
        }

        test("shows the signed in user") {
            val user = signedIn("STUDENT")

            mockMvc.get("/api/v1/me") { bearer(user) }.andExpect {
                status { isOk() }
                jsonPath("$.id") { value(externalId.toString()) }
                jsonPath("$.email") { value("ana@test.com") }
                jsonPath("$.fullName") { value("Ana Souza") }
            }
        }

        test("rejects an unsupported api version") {
            val user = signedIn("STUDENT")

            mockMvc.get("/api/v2/me") { bearer(user) }.andExpect {
                status { isBadRequest() }
                content { contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON) }
            }
        }

        test("lists the terms the student is enrolled in") {
            val user = signedIn("STUDENT")
            every { academicTermService.findAllEnrolledBy(user) } returns listOf(
                AcademicTerm(institution = udesc, period = AcademicPeriod(2026, 2)),
                AcademicTerm(institution = udesc, period = AcademicPeriod(2026, 1)),
            )

            mockMvc.get("/api/v1/me/terms") { bearer(user) }.andExpect {
                status { isOk() }
                jsonPath("$[0].year") { value(2026) }
                jsonPath("$[0].term") { value(2) }
                jsonPath("$[1].term") { value(1) }
            }
        }

        test("lists the enrollments of a term") {
            val user = signedIn("STUDENT")
            every { enrollmentService.findAll(user, AcademicPeriod(2026, 1)) } returns listOf(enrollment(user))

            mockMvc.get("/api/v1/me/terms/2026/1/enrollments") { bearer(user) }.andExpect {
                status { isOk() }
                jsonPath("$[0].subject") { value("Banco de Dados") }
                jsonPath("$[0].classSection") { value("ESO-1") }
                jsonPath("$[0].courseLoad") { value(72) }
                jsonPath("$[0].finalGrade") { value(8.5) }
                jsonPath("$[0].absences") { value(2) }
                jsonPath("$[0].attendancePercentage") { value(95.0) }
                jsonPath("$[0].result") { value("Aprovado") }
            }
        }

        test("syncs one term from SIGA") {
            val user = signedIn("STUDENT")
            every { retrieveService.retrieveForUser(any(), user) } returns listOf(AcademicTermOutput(2026, 1))

            mockMvc.post("/api/v1/me/syncs") { bearer(user); json("""{"year":2026,"term":1}""") }
                .andExpect {
                    status { isOk() }
                    jsonPath("$[0].year") { value(2026) }
                }
        }

        test("syncs every term from SIGA") {
            val user = signedIn("STUDENT")
            every { retrieveService.fullRetrieve(user) } returns listOf(AcademicTermOutput(2026, 1))

            mockMvc.post("/api/v1/me/syncs") { bearer(user); json("""{"scope":"full"}""") }
                .andExpect { status { isOk() } }

            verify { retrieveService.fullRetrieve(user) }
        }

        test("asks for the term when syncing one term") {
            val user = signedIn("STUDENT")

            mockMvc.post("/api/v1/me/syncs") { bearer(user); json("""{"year":2026}""") }
                .andExpect {
                    status { isBadRequest() }
                    content { contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON) }
                }
        }

        test("pages the notifications of the signed in user") {
            val user = signedIn("STUDENT")
            val createdAt = Instant.parse("2026-11-20T12:00:00Z")
            every { notificationService.listNotifications(user, PageRequest.of(1, 5)) } returns
                PageImpl(listOf(NotificationOutput(7, "Nova nota", createdAt)), PageRequest.of(1, 5), 6)

            mockMvc.get("/api/v1/me/notifications?page=1&size=5") { bearer(user) }.andExpect {
                status { isOk() }
                jsonPath("$.content[0].id") { value(7) }
                jsonPath("$.content[0].message") { value("Nova nota") }
                jsonPath("$.content[0].createdAt") { value("2026-11-20T12:00:00Z") }
                jsonPath("$.page.totalElements") { value(6) }
            }
        }

        test("forbids students from broadcasting") {
            val user = signedIn("STUDENT")

            mockMvc.post("/api/v1/notifications") { bearer(user); json("""{"message":"Oi"}""") }
                .andExpect { status { isForbidden() } }
        }

        test("lets admins broadcast") {
            val user = signedIn("ADMIN")

            mockMvc.post("/api/v1/notifications") { bearer(user); json("""{"message":"Oi"}""") }
                .andExpect { status { isAccepted() } }

            verify { notificationService.createNotificationToAll("Oi") }
        }

        test("lets admins notify one user") {
            val user = signedIn("ADMIN")
            every { notificationService.createNotificationToUser(UserId(externalId), "Oi") } returns
                NotificationOutput(7, "Oi", Instant.parse("2026-11-20T12:00:00Z"))

            mockMvc.post("/api/v1/users/$externalId/notifications") { bearer(user); json("""{"message":"Oi"}""") }
                .andExpect {
                    status { isCreated() }
                    jsonPath("$.id") { value(7) }
                }
        }

        test("answers 404 when notifying an unknown user") {
            val user = signedIn("ADMIN")
            val unknown = UserId.generate()
            every { notificationService.createNotificationToUser(unknown, "Oi") } throws UserNotFoundException(unknown)

            mockMvc.post("/api/v1/users/$unknown/notifications") { bearer(user); json("""{"message":"Oi"}""") }
                .andExpect {
                    status { isNotFound() }
                    content { contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON) }
                }
        }

        test("refuses a token for a user that no longer exists") {
            val user = signedIn("STUDENT")
            every { userDetailsService.loadUserByUsername("ana@test.com") } throws UsernameNotFoundException("gone")

            mockMvc.get("/api/v1/me") { bearer(user) }.andExpect { status { isUnauthorized() } }
        }
    }
}
