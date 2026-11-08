package br.com.mariofronza.sigabot.usecases.misc.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*

@Component
class JWTUtil(
    @param:Value("\${jwt.secret}")
    private val secret: String,
) {

    init {
        require(secret.toByteArray().size >= MIN_SECRET_BYTES) { "jwt.secret must have at least $MIN_SECRET_BYTES bytes" }
    }

    private val algorithm = Algorithm.HMAC256(secret.toByteArray())

    fun generateAccessToken(user: UserDetails, requestUrl: String): String = JWT.create()
        .withSubject(user.username)
        .withExpiresAt(Date(System.currentTimeMillis() + 10 * 60 * 1000))
        .withIssuer(requestUrl)
        .withClaim("roles", user.authorities.map { obj: GrantedAuthority -> obj.authority })
        .withClaim(TOKEN_TYPE, ACCESS)
        .sign(algorithm)

    fun generateRefreshToken(user: UserDetails, requestUrl: String): String = JWT.create()
        .withSubject(user.username)
        .withExpiresAt(getDateNextYear())
        .withIssuer(requestUrl)
        .withClaim(TOKEN_TYPE, REFRESH)
        .sign(algorithm)

    fun getDateNextYear(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, 1)
        return calendar.time
    }

    fun decodeAccessToken(token: String): DecodedJWT = verify(token, ACCESS)

    fun decodeRefreshToken(token: String): DecodedJWT = verify(token, REFRESH)

    private fun verify(token: String, type: String): DecodedJWT =
        JWT.require(algorithm).withClaim(TOKEN_TYPE, type).build().verify(token)

    companion object {
        const val TOKEN_TYPE = "type"
        const val ACCESS = "access"
        const val REFRESH = "refresh"
        const val MIN_SECRET_BYTES = 32
    }
}
