package br.com.mariofronza.sigabot.entity

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.session.RefreshToken
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.entity.user.UserId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Duration
import java.time.Instant

class SessionTest : FunSpec({

    val now = Instant.parse("2026-11-20T12:00:00Z")
    val ttl = Duration.ofDays(30)

    test("issues a random token and keeps only its hash") {
        val (token, raw) = RefreshToken.issue(UserId.generate(), familyId = null, now, ttl)

        token.tokenHash shouldBe RefreshToken.hash(raw)
        RefreshToken.issue(token.userId, null, now, ttl).second shouldNotBe raw
        token.expiresAt shouldBe now.plus(ttl)
    }

    test("keeps the family when rotating") {
        val (first, _) = RefreshToken.issue(UserId.generate(), familyId = null, now, ttl)
        val (second, _) = RefreshToken.issue(first.userId, first.familyId, now, ttl)

        second.familyId shouldBe first.familyId
    }

    test("is usable until it expires or is revoked") {
        val (token, _) = RefreshToken.issue(UserId.generate(), familyId = null, now, ttl)

        token.isUsableAt(now.plus(ttl).minusSeconds(1)) shouldBe true
        token.isUsableAt(now.plus(ttl)) shouldBe false
        token.revoke(now).isUsableAt(now) shouldBe false
    }

    test("grants a role once") {
        val user = User.registerStudent(Email.of("ana@test.com"), "hash", AcademicAccount(InstitutionCode.UDESC, "l", byteArrayOf(1), "p"))

        user.grant(Role.ADMIN).grant(Role.ADMIN).roles shouldBe setOf(Role.STUDENT, Role.ADMIN)
    }
})
