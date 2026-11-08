package br.com.mariofronza.sigabot.usecases.misc.utils

import com.auth0.jwt.exceptions.JWTVerificationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import org.springframework.security.core.userdetails.User

class JWTUtilTest : FunSpec({

    val jwtUtil = JWTUtil("test-secret-test-secret-test-secret-0123")
    val user = User.withUsername("student@test.com").password("hash").authorities("STUDENT").build()

    test("rejects a refresh token used as access token") {
        shouldThrow<JWTVerificationException> {
            jwtUtil.decodeAccessToken(jwtUtil.generateRefreshToken(user, "/sessions"))
        }
    }

    test("rejects an access token used as refresh token") {
        shouldThrow<JWTVerificationException> {
            jwtUtil.decodeRefreshToken(jwtUtil.generateAccessToken(user, "/sessions"))
        }
    }

    test("refuses a secret shorter than 32 bytes") {
        shouldThrow<IllegalArgumentException> { JWTUtil("short") }
    }
})
