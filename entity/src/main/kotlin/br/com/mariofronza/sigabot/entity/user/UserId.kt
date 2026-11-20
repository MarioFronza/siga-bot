package br.com.mariofronza.sigabot.entity.user

import java.security.SecureRandom
import java.time.Clock
import java.util.UUID

@JvmInline
value class UserId(val value: UUID) {

    override fun toString() = value.toString()

    companion object {
        private val random = SecureRandom()

        fun generate(clock: Clock = Clock.systemUTC()): UserId {
            val millis = clock.millis()
            val mostSignificant = (millis shl 16) or 0x7000L or (random.nextLong() and 0x0FFFL)
            val leastSignificant = (random.nextLong() and 0x3FFFFFFFFFFFFFFFL) or Long.MIN_VALUE
            return UserId(UUID(mostSignificant, leastSignificant))
        }
    }
}
