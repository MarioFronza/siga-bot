package br.com.mariofronza.sigabot.usecases.identity

import br.com.mariofronza.sigabot.entity.session.InvalidRefreshTokenException
import br.com.mariofronza.sigabot.entity.session.RefreshToken
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.usecases.identity.port.AccessTokenIssuer
import br.com.mariofronza.sigabot.usecases.identity.port.RefreshTokenRepository
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import br.com.mariofronza.sigabot.usecases.shared.TransactionRunner
import java.time.Clock
import java.time.Duration
import java.util.UUID

data class Session(val accessToken: String, val refreshToken: String, val userId: UserId)

class OpenSession(
    private val tokens: RefreshTokenRepository,
    private val accessTokens: AccessTokenIssuer,
    private val clock: Clock,
    private val refreshTtl: Duration = Duration.ofDays(30),
) {

    operator fun invoke(user: User): Session = open(user, familyId = null)

    internal fun open(user: User, familyId: UUID?): Session {
        val (token, raw) = RefreshToken.issue(user.id, familyId, clock.instant(), refreshTtl)
        tokens.save(token)
        return Session(accessTokens.issue(user), raw, user.id)
    }
}

class RefreshSession(
    private val tokens: RefreshTokenRepository,
    private val users: UserRepository,
    private val openSession: OpenSession,
    private val transaction: TransactionRunner,
    private val clock: Clock,
) {

    operator fun invoke(rawRefreshToken: String): Session {
        val now = clock.instant()
        val outcome = transaction.inTransaction {
            val token = tokens.findByHash(RefreshToken.hash(rawRefreshToken)) ?: return@inTransaction Refused("unknown token")
            if (token.isRevoked) {
                tokens.revokeFamily(token.familyId, now)
                return@inTransaction Refused("token reused, its session was revoked")
            }
            if (!token.isUsableAt(now)) return@inTransaction Refused("token expired")
            val user = users.findById(token.userId)?.takeIf { it.isActive } ?: return@inTransaction Refused("user is gone")
            tokens.save(token.revoke(now))
            Rotated(openSession.open(user, token.familyId))
        }
        return when (outcome) {
            is Rotated -> outcome.session
            is Refused -> throw InvalidRefreshTokenException(outcome.reason)
        }
    }

    private sealed interface Outcome
    private data class Rotated(val session: Session) : Outcome
    private data class Refused(val reason: String) : Outcome
}

class RevokeSession(
    private val tokens: RefreshTokenRepository,
    private val clock: Clock,
) {

    operator fun invoke(rawRefreshToken: String) {
        tokens.findByHash(RefreshToken.hash(rawRefreshToken))?.let { tokens.revokeFamily(it.familyId, clock.instant()) }
    }
}
