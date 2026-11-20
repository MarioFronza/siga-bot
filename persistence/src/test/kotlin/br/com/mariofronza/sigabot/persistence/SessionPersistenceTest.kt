package br.com.mariofronza.sigabot.persistence

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.session.RefreshToken
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.session.JooqRefreshTokenRepository
import br.com.mariofronza.sigabot.persistence.user.JooqUserRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.jooq.test.autoconfigure.JooqTest
import org.springframework.context.annotation.Import
import java.time.Duration
import java.time.Instant

@JooqTest
@ImportAutoConfiguration(FlywayAutoConfiguration::class)
@Import(JooqUserRepository::class, JooqRefreshTokenRepository::class)
class SessionPersistenceTest : FunSpec() {

    @Autowired lateinit var users: JooqUserRepository
    @Autowired lateinit var tokens: JooqRefreshTokenRepository

    private val now = Instant.parse("2026-11-20T12:00:00Z")

    private fun student() = users.save(
        User.registerStudent(Email.of("ana@test.com"), "hash", AcademicAccount(InstitutionCode.UDESC, "l", byteArrayOf(1), "p"))
    )

    init {
        extension(SpringExtension())

        test("finds a refresh token by its hash") {
            val (token, raw) = RefreshToken.issue(student().id, null, now, Duration.ofDays(30))
            tokens.save(token)

            val found = tokens.findByHash(RefreshToken.hash(raw)).shouldNotBeNull()
            found.familyId shouldBe token.familyId
            found.expiresAt shouldBe token.expiresAt
            found.revokedAt.shouldBeNull()
        }

        test("revokes every token of a family") {
            val user = student()
            val (first, firstRaw) = RefreshToken.issue(user.id, null, now, Duration.ofDays(30))
            val (second, secondRaw) = RefreshToken.issue(user.id, first.familyId, now, Duration.ofDays(30))
            tokens.save(first)
            tokens.save(second)

            tokens.revokeFamily(first.familyId, now)

            tokens.findByHash(RefreshToken.hash(firstRaw)).shouldNotBeNull().isRevoked shouldBe true
            tokens.findByHash(RefreshToken.hash(secondRaw)).shouldNotBeNull().isRevoked shouldBe true
        }

        test("stores a revocation made by rotating") {
            val (token, raw) = RefreshToken.issue(student().id, null, now, Duration.ofDays(30))
            tokens.save(token)
            tokens.save(token.revoke(now))

            tokens.findByHash(RefreshToken.hash(raw)).shouldNotBeNull().revokedAt shouldBe now
        }

        test("stores a role granted to an existing user") {
            val user = student()
            users.save(user.grant(Role.ADMIN))

            users.findById(user.id).shouldNotBeNull().roles shouldBe setOf(Role.STUDENT, Role.ADMIN)
        }
    }
}
