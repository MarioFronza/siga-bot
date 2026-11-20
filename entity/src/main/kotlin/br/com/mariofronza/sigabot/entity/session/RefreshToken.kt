package br.com.mariofronza.sigabot.entity.session

import br.com.mariofronza.sigabot.entity.user.UserId
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.UUID

class RefreshToken(
    val id: UUID,
    val userId: UserId,
    val familyId: UUID,
    val tokenHash: ByteArray,
    val expiresAt: Instant,
    val revokedAt: Instant? = null,
) {

    val isRevoked: Boolean get() = revokedAt != null

    fun isUsableAt(now: Instant) = !isRevoked && now.isBefore(expiresAt)

    fun revoke(now: Instant) = RefreshToken(id, userId, familyId, tokenHash, expiresAt, revokedAt ?: now)

    companion object {
        private const val TOKEN_BYTES = 32
        private val random = SecureRandom()
        private val encoder = Base64.getUrlEncoder().withoutPadding()

        fun issue(userId: UserId, familyId: UUID?, now: Instant, ttl: Duration): Pair<RefreshToken, String> {
            val raw = encoder.encodeToString(ByteArray(TOKEN_BYTES).also(random::nextBytes))
            val token = RefreshToken(UUID.randomUUID(), userId, familyId ?: UUID.randomUUID(), hash(raw), now.plus(ttl))
            return token to raw
        }

        fun hash(raw: String): ByteArray = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
    }
}
