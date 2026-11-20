package br.com.mariofronza.sigabot.usecases.identity

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.session.InvalidRefreshTokenException
import br.com.mariofronza.sigabot.entity.session.RefreshToken
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.identity.port.RefreshTokenRepository
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import br.com.mariofronza.sigabot.usecases.shared.TransactionRunner
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class SessionsTest : FunSpec({

    class InMemoryTokens : RefreshTokenRepository {
        val all = mutableMapOf<UUID, RefreshToken>()
        override fun save(token: RefreshToken) { all[token.id] = token }
        override fun findByHash(tokenHash: ByteArray) = all.values.firstOrNull { it.tokenHash.contentEquals(tokenHash) }
        override fun revokeFamily(familyId: UUID, now: Instant) {
            all.replaceAll { _, token -> if (token.familyId == familyId) token.revoke(now) else token }
        }
    }

    val now = Instant.parse("2026-11-20T12:00:00Z")
    val clock = Clock.fixed(now, ZoneOffset.UTC)
    val transaction = object : TransactionRunner {
        override fun <T> inTransaction(block: () -> T): T = block()
    }
    val user = User.registerStudent(Email.of("ana@test.com"), "hash", AcademicAccount(InstitutionCode.UDESC, "l", byteArrayOf(1), "p"))
    val users: UserRepository = mockk()
    every { users.findById(user.id) } returns user

    fun sessions(tokens: RefreshTokenRepository): Pair<OpenSession, RefreshSession> {
        val open = OpenSession(tokens, { "access-for-${it.email}" }, clock)
        return open to RefreshSession(tokens, users, open, transaction, clock)
    }

    test("opens a session with an access token and a refresh token") {
        val (open, _) = sessions(InMemoryTokens())

        val session = open(user)

        session.accessToken shouldBe "access-for-ana@test.com"
        session.userId shouldBe user.id
    }

    test("rotates the refresh token and refuses the old one afterwards") {
        val tokens = InMemoryTokens()
        val (open, refresh) = sessions(tokens)
        val first = open(user)

        val second = refresh(first.refreshToken)

        second.refreshToken shouldNotBe first.refreshToken
        refresh(second.refreshToken).userId shouldBe user.id
    }

    test("revokes the whole session when a rotated token is reused") {
        val tokens = InMemoryTokens()
        val (open, refresh) = sessions(tokens)
        val first = open(user)
        val second = refresh(first.refreshToken)

        shouldThrow<InvalidRefreshTokenException> { refresh(first.refreshToken) }
        shouldThrow<InvalidRefreshTokenException> { refresh(second.refreshToken) }
    }

    test("refuses an unknown refresh token") {
        val (_, refresh) = sessions(InMemoryTokens())

        shouldThrow<InvalidRefreshTokenException> { refresh("forged") }
    }

    test("refuses an expired refresh token") {
        val tokens = InMemoryTokens()
        val (open, _) = sessions(tokens)
        val session = open(user)
        val later = RefreshSession(tokens, users, open, transaction, Clock.fixed(now.plusSeconds(31L * 24 * 3600), ZoneOffset.UTC))

        shouldThrow<InvalidRefreshTokenException> { later(session.refreshToken) }
    }

    test("logs out by revoking the session of a refresh token") {
        val tokens = InMemoryTokens()
        val (open, refresh) = sessions(tokens)
        val session = open(user)

        RevokeSession(tokens, clock)(session.refreshToken)

        shouldThrow<InvalidRefreshTokenException> { refresh(session.refreshToken) }
    }
})
