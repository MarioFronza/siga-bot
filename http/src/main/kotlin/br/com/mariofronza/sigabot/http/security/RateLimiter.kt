package br.com.mariofronza.sigabot.http.security

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class RateLimiter(
    private val limit: Int,
    private val window: Duration,
    private val clock: Clock,
) {

    private data class Window(val startedAt: Instant, val count: Int)

    private val windows = ConcurrentHashMap<String, Window>()

    data class Admission(val allowed: Boolean, val retryAfter: Duration)

    fun acquire(key: String): Admission {
        val now = clock.instant()
        if (windows.size > MAX_TRACKED_KEYS) windows.values.removeIf { expired(it, now) }
        val current = checkNotNull(windows.compute(key) { _, window ->
            if (window == null || expired(window, now)) Window(now, 1) else window.copy(count = window.count + 1)
        }) { "compute always returns a window" }
        return Admission(allowed = current.count <= limit, retryAfter = Duration.between(now, current.startedAt.plus(window)))
    }

    private fun expired(window: Window, now: Instant) = !now.isBefore(window.startedAt.plus(this.window))

    companion object {
        private const val MAX_TRACKED_KEYS = 10_000
    }
}
