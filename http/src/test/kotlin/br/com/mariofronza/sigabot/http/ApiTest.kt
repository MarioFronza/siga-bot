package br.com.mariofronza.sigabot.http

import br.com.mariofronza.sigabot.entity.session.InvalidRefreshTokenException
import br.com.mariofronza.sigabot.usecases.identity.Session
import br.com.mariofronza.sigabot.usecases.identity.RevokeSession
import br.com.mariofronza.sigabot.usecases.identity.RefreshSession
import br.com.mariofronza.sigabot.usecases.identity.OpenSession
import br.com.mariofronza.sigabot.usecases.academic.port.SigaUnavailableException
import br.com.mariofronza.sigabot.entity.notification.Notification
import br.com.mariofronza.sigabot.usecases.academic.ListEnrolledPeriods
import br.com.mariofronza.sigabot.usecases.academic.ListPeriodEnrollments
import br.com.mariofronza.sigabot.usecases.academic.SyncStudentGrades
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import br.com.mariofronza.sigabot.usecases.identity.RegisterStudent
import br.com.mariofronza.sigabot.usecases.notification.BroadcastNotification
import br.com.mariofronza.sigabot.usecases.notification.ListNotifications
import br.com.mariofronza.sigabot.usecases.notification.NotifyStudent
import br.com.mariofronza.sigabot.usecases.shared.Page
import br.com.mariofronza.sigabot.usecases.shared.PageRequest
import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.enrollment.Attendance
import br.com.mariofronza.sigabot.entity.enrollment.Grade
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.enrollment.Enrollment
import br.com.mariofronza.sigabot.entity.catalog.SubjectOffering
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.http.security.SecurityConfig
import br.com.mariofronza.sigabot.http.security.UserDetailsImpl
import br.com.mariofronza.sigabot.http.security.JWTUtil
import br.com.mariofronza.sigabot.entity.user.AcademicAccountAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.EmailAlreadyRegisteredException
import br.com.mariofronza.sigabot.entity.user.UserNotFoundException
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
import org.hamcrest.Matchers.containsInAnyOrder
import java.time.Instant
import java.util.UUID

@WebMvcTest
@Import(SecurityConfig::class, JWTUtil::class, ApiTest.Mocks::class)
@TestPropertySource(properties = ["jwt.secret=test-secret-test-secret-test-secret-0123", "security.rate-limit.per-minute=1000"])
class ApiTest : FunSpec() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jwtUtil: JWTUtil
    @Autowired lateinit var userDetailsService: UserDetailsService
    @Autowired lateinit var registerStudent: RegisterStudent
    @Autowired lateinit var syncStudentGrades: SyncStudentGrades
    @Autowired lateinit var listEnrolledPeriods: ListEnrolledPeriods
    @Autowired lateinit var listPeriodEnrollments: ListPeriodEnrollments
    @Autowired lateinit var listNotifications: ListNotifications
    @Autowired lateinit var broadcastNotification: BroadcastNotification
    @Autowired lateinit var notifyStudent: NotifyStudent
    @Autowired lateinit var openSession: OpenSession
    @Autowired lateinit var refreshSession: RefreshSession
    @Autowired lateinit var revokeSession: RevokeSession

    @TestConfiguration
    class Mocks {
        @Bean fun userDetailsService(): UserDetailsService = mockk()
        @Bean fun registerStudent(): RegisterStudent = mockk()
        @Bean fun syncStudentGrades(): SyncStudentGrades = mockk()
        @Bean fun listEnrolledPeriods(): ListEnrolledPeriods = mockk()
        @Bean fun listPeriodEnrollments(): ListPeriodEnrollments = mockk()
        @Bean fun listNotifications(): ListNotifications = mockk()
        @Bean fun broadcastNotification(): BroadcastNotification = mockk(relaxed = true)
        @Bean fun notifyStudent(): NotifyStudent = mockk()
        @Bean fun openSession(): OpenSession = mockk()
        @Bean fun refreshSession(): RefreshSession = mockk()
        @Bean fun revokeSession(): RevokeSession = mockk(relaxed = true)
    }

    private val externalId = UUID.fromString("01a1f973-13ad-7a5c-80df-bf421ada5df1")
    private val passwordHash = BCryptPasswordEncoder().encode("app-password").shouldNotBeNull()

    private fun signedIn(vararg roles: Role): User {
        val user = User(
            id = UserId(externalId),
            isActive = true,
            fullName = "Ana Souza",
            email = Email.of("ana@test.com"),
            passwordHash = passwordHash,
            roles = roles.toSet(),
            academicAccounts = emptyList(),
        )
        every { userDetailsService.loadUserByUsername("ana@test.com") } returns UserDetailsImpl(user)
        return user
    }

    private fun MockHttpServletRequestDsl.bearer(user: User) {
        header("Authorization", "Bearer ${jwtUtil.generateAccessToken(UserDetailsImpl(user))}")
    }

    private fun MockHttpServletRequestDsl.json(body: String) {
        contentType = APPLICATION_JSON
        content = body
    }

    private fun enrollment(user: User): Enrollment {
        return Enrollment(
            studentId = user.id,
            offering = SubjectOffering(InstitutionCode.UDESC, AcademicPeriod(2026, 1), "ESO", "ESO-1", "Banco de Dados", courseLoad = 72),
            grade = Grade(8.5),
            absences = 2,
            attendance = Attendance(95.0),
            result = "Aprovado",
        )
    }

    init {
        extension(SpringExtension())

        test("signs a student up and points at the profile") {
            every { registerStudent(any()) } returns User(id = UserId(externalId), isActive = true, email = Email.of("ana@test.com"), passwordHash = "hash", roles = setOf(Role.STUDENT), academicAccounts = emptyList())

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
                json("""{"email":"not-an-email","password":"","sigaCredential":{"cpf":"","password":"siga"}}""")
            }.andExpect {
                status { isBadRequest() }
                content { contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON) }
                jsonPath("$.errors[*].field") { value(containsInAnyOrder("email", "password", "sigaCredential.cpf")) }
                jsonPath("$.errors[0].message") { isNotEmpty() }
            }
        }

        test("answers 409 when the email is already registered") {
            every { registerStudent(any()) } throws EmailAlreadyRegisteredException()

            mockMvc.post("/api/v1/users") {
                json("""{"email":"ana@test.com","password":"app-password","sigaCredential":{"cpf":"000","password":"siga"}}""")
            }.andExpect {
                status { isConflict() }
                content { contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON) }
                jsonPath("$.type") { value("urn:siga-bot:problem:email-already-registered") }
                jsonPath("$.field") { value("email") }
            }
        }

        test("answers 409 on the cpf field when the SIGA credential is already registered") {
            every { registerStudent(any()) } throws AcademicAccountAlreadyRegisteredException()

            mockMvc.post("/api/v1/users") {
                json("""{"email":"ana@test.com","password":"app-password","sigaCredential":{"cpf":"000","password":"siga"}}""")
            }.andExpect {
                status { isConflict() }
                jsonPath("$.type") { value("urn:siga-bot:problem:siga-credential-already-registered") }
                jsonPath("$.field") { value("cpf") }
            }
        }

        test("opens a session with the app credentials") {
            val user = signedIn(Role.STUDENT)
            every { openSession(user) } returns Session("access", "refresh", user.id)

            mockMvc.post("/api/v1/sessions") { json("""{"email":"ana@test.com","password":"app-password"}""") }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.accessToken") { value("access") }
                    jsonPath("$.refreshToken") { value("refresh") }
                    jsonPath("$.userId") { value(externalId.toString()) }
                }
        }

        test("refuses a session with a wrong password") {
            signedIn(Role.STUDENT)

            mockMvc.post("/api/v1/sessions") { json("""{"email":"ana@test.com","password":"wrong"}""") }
                .andExpect { status { isUnauthorized() } }
        }

        test("refreshes a session from the refresh token in the body") {
            val user = signedIn(Role.STUDENT)
            every { refreshSession("current") } returns Session("new-access", "next", user.id)

            mockMvc.post("/api/v1/sessions/refresh") { json("""{"refreshToken":"current"}""") }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.accessToken") { value("new-access") }
                    jsonPath("$.refreshToken") { value("next") }
                }
        }

        test("logs out by revoking the session of a refresh token") {
            mockMvc.post("/api/v1/sessions/revoke") { json("""{"refreshToken":"current"}""") }
                .andExpect { status { isNoContent() } }

            verify { revokeSession("current") }
        }

        test("answers 401 as a problem when the refresh token is rejected") {
            every { refreshSession("invalid") } throws InvalidRefreshTokenException("unknown token")

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
            val user = signedIn(Role.STUDENT)

            mockMvc.get("/api/v1/me") { bearer(user) }.andExpect {
                status { isOk() }
                jsonPath("$.id") { value(externalId.toString()) }
                jsonPath("$.email") { value("ana@test.com") }
                jsonPath("$.fullName") { value("Ana Souza") }
            }
        }

        test("shows the roles of the signed in user") {
            val user = signedIn(Role.STUDENT, Role.ADMIN)

            mockMvc.get("/api/v1/me") { bearer(user) }.andExpect {
                status { isOk() }
                jsonPath("$.roles") { value(containsInAnyOrder("STUDENT", "ADMIN")) }
            }
        }

        test("rejects an unsupported api version") {
            val user = signedIn(Role.STUDENT)

            mockMvc.get("/api/v2/me") { bearer(user) }.andExpect {
                status { isBadRequest() }
                content { contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON) }
            }
        }

        test("lists the terms the student is enrolled in") {
            val user = signedIn(Role.STUDENT)
            every { listEnrolledPeriods(user.id) } returns listOf(AcademicPeriod(2026, 2), AcademicPeriod(2026, 1))

            mockMvc.get("/api/v1/me/terms") { bearer(user) }.andExpect {
                status { isOk() }
                jsonPath("$[0].year") { value(2026) }
                jsonPath("$[0].term") { value(2) }
                jsonPath("$[1].term") { value(1) }
            }
        }

        test("lists the enrollments of a term") {
            val user = signedIn(Role.STUDENT)
            every { listPeriodEnrollments(user.id, AcademicPeriod(2026, 1)) } returns listOf(enrollment(user))

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
            val user = signedIn(Role.STUDENT)
            every { syncStudentGrades(user, SyncScope.Period(AcademicPeriod(2026, 1))) } returns listOf(AcademicPeriod(2026, 1))

            mockMvc.post("/api/v1/me/syncs") { bearer(user); json("""{"year":2026,"term":1}""") }
                .andExpect {
                    status { isOk() }
                    jsonPath("$[0].year") { value(2026) }
                }
        }

        test("syncs every term from SIGA") {
            val user = signedIn(Role.STUDENT)
            every { syncStudentGrades(user, SyncScope.Everything) } returns listOf(AcademicPeriod(2026, 1))

            mockMvc.post("/api/v1/me/syncs") { bearer(user); json("""{"scope":"full"}""") }
                .andExpect { status { isOk() } }

            verify { syncStudentGrades(user, SyncScope.Everything) }
        }

        test("answers 503 as a problem when SIGA is unavailable during a sync") {
            val user = signedIn(Role.STUDENT)
            every { syncStudentGrades(user, SyncScope.Period(AcademicPeriod(2026, 1))) } throws SigaUnavailableException("SIGA did not answer")

            mockMvc.post("/api/v1/me/syncs") { bearer(user); json("""{"year":2026,"term":1}""") }
                .andExpect {
                    status { isServiceUnavailable() }
                    content { contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON) }
                }
        }

        test("asks for the term when syncing one term") {
            val user = signedIn(Role.STUDENT)

            mockMvc.post("/api/v1/me/syncs") { bearer(user); json("""{"year":2026}""") }
                .andExpect {
                    status { isBadRequest() }
                    content { contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON) }
                }
        }

        test("pages the notifications of the signed in user") {
            val user = signedIn(Role.STUDENT)
            val createdAt = Instant.parse("2026-11-20T12:00:00Z")
            every { listNotifications(user.id, PageRequest(1, 5)) } returns
                Page(listOf(Notification(7, user.id, "Nova nota", createdAt)), PageRequest(1, 5), 6)

            mockMvc.get("/api/v1/me/notifications?page=1&size=5") { bearer(user) }.andExpect {
                status { isOk() }
                jsonPath("$.content[0].id") { value(7) }
                jsonPath("$.content[0].message") { value("Nova nota") }
                jsonPath("$.content[0].createdAt") { value("2026-11-20T12:00:00Z") }
                jsonPath("$.page.totalElements") { value(6) }
            }
        }

        test("forbids students from broadcasting") {
            val user = signedIn(Role.STUDENT)

            mockMvc.post("/api/v1/notifications") { bearer(user); json("""{"message":"Oi"}""") }
                .andExpect { status { isForbidden() } }
        }

        test("lets admins broadcast") {
            val user = signedIn(Role.ADMIN)

            mockMvc.post("/api/v1/notifications") { bearer(user); json("""{"message":"Oi"}""") }
                .andExpect { status { isAccepted() } }

            verify { broadcastNotification("Oi") }
        }

        test("lets admins notify one user") {
            val user = signedIn(Role.ADMIN)
            every { notifyStudent(UserId(externalId), "Oi") } returns
                Notification(7, UserId(externalId), "Oi", Instant.parse("2026-11-20T12:00:00Z"))

            mockMvc.post("/api/v1/users/$externalId/notifications") { bearer(user); json("""{"message":"Oi"}""") }
                .andExpect {
                    status { isCreated() }
                    jsonPath("$.id") { value(7) }
                }
        }

        test("answers 404 when notifying an unknown user") {
            val user = signedIn(Role.ADMIN)
            val unknown = UserId.generate()
            every { notifyStudent(unknown, "Oi") } throws UserNotFoundException(unknown)

            mockMvc.post("/api/v1/users/$unknown/notifications") { bearer(user); json("""{"message":"Oi"}""") }
                .andExpect {
                    status { isNotFound() }
                    content { contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON) }
                }
        }

        test("refuses a token for a user that no longer exists") {
            val user = signedIn(Role.STUDENT)
            every { userDetailsService.loadUserByUsername("ana@test.com") } throws UsernameNotFoundException("gone")

            mockMvc.get("/api/v1/me") { bearer(user) }.andExpect { status { isUnauthorized() } }
        }
    }
}
