package br.com.mariofronza.sigabot.usecases.notification

import br.com.mariofronza.sigabot.usecases.notification.port.Outbox
import br.com.mariofronza.sigabot.usecases.notification.port.OutboxMessage
import br.com.mariofronza.sigabot.usecases.notification.port.PushSender
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Duration

class DeliverOutbox(
    private val outbox: Outbox,
    private val pushSender: PushSender,
    private val clock: Clock,
    private val settings: Settings = Settings(),
) {

    data class Settings(
        val batchSize: Int = 50,
        val lease: Duration = Duration.ofMinutes(5),
        val firstRetry: Duration = Duration.ofSeconds(30),
        val maxRetry: Duration = Duration.ofHours(1),
    )

    operator fun invoke(): Int =
        outbox.claimPending(clock.instant(), settings.batchSize, settings.lease, MAX_ATTEMPTS).count(::deliver)

    private fun deliver(message: OutboxMessage): Boolean = try {
        pushSender.sendTo(message.recipient, message.message)
        outbox.delivered(message.id)
        true
    } catch (e: Exception) {
        logger.warn("Push {} failed (attempt {}), it stays in the outbox", message.id, message.attempts + 1, e)
        outbox.failed(message.id, clock.instant().plus(retryDelay(message.attempts + 1)))
        false
    }

    internal fun retryDelay(attempt: Int): Duration {
        val doublings = (attempt - 1).coerceIn(0, 20)
        return settings.firstRetry.multipliedBy(1L shl doublings).coerceAtMost(settings.maxRetry)
    }

    companion object {
        const val MAX_ATTEMPTS = 10
        private val logger = LoggerFactory.getLogger(DeliverOutbox::class.java)
    }
}
