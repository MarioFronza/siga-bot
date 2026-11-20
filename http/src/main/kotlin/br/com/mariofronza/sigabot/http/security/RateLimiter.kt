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

    fun tryAcquire(key: String): Boolean {
        val now = clock.instant()
        if (windows.size > MAX_TRACKED_KEYS) windows.values.removeIf { expired(it, now) }
        val current = windows.compute(key) { _, window ->
            if (window == null || expired(window, now)) Window(now, 1) else window.copy(count = window.count + 1)
        }
        return checkNotNull(current) { "compute always returns a window" }.count <= limit
    }

    private fun expired(window: Window, now: Instant) = !now.isBefore(window.startedAt.plus(this.window))

    companion object {
        private const val MAX_TRACKED_KEYS = 10_000
    }
}
