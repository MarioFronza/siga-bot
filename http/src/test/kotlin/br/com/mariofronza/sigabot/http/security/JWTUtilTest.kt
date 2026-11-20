package br.com.mariofronza.sigabot.http.security

import io.kotest.matchers.shouldBe
import com.auth0.jwt.exceptions.JWTVerificationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import org.springframework.security.core.userdetails.User

class JWTUtilTest : FunSpec({

    val jwtUtil = JWTUtil("test-secret-test-secret-test-secret-0123")
    val user = User.withUsername("student@test.com").password("hash").authorities("STUDENT").build()

    test("reads back the subject of an access token it issued") {
        jwtUtil.decodeAccessToken(jwtUtil.generateAccessToken(user)).subject shouldBe "student@test.com"
    }

    test("rejects an access token signed with another secret") {
        val other = JWTUtil("another-secret-another-secret-0123456")

        shouldThrow<JWTVerificationException> { jwtUtil.decodeAccessToken(other.generateAccessToken(user)) }
    }

    test("refuses a secret shorter than 32 bytes") {
        shouldThrow<IllegalArgumentException> { JWTUtil("short") }
    }
})
