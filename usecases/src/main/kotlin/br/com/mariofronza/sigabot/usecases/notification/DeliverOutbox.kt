package br.com.mariofronza.sigabot.usecases.notification

import br.com.mariofronza.sigabot.usecases.notification.port.Outbox
import br.com.mariofronza.sigabot.usecases.notification.port.PushSender
import org.slf4j.LoggerFactory

class DeliverOutbox(
    private val outbox: Outbox,
    private val pushSender: PushSender,
    private val batchSize: Int = 50,
    private val maxAttempts: Int = MAX_ATTEMPTS,
) {

    operator fun invoke(): Int = outbox.pending(batchSize, maxAttempts).count { message ->
        try {
            pushSender.sendTo(message.recipient, message.message)
            outbox.delivered(message.id)
            true
        } catch (e: Exception) {
            logger.warn("Push {} failed, it stays in the outbox", message.id, e)
            outbox.failed(message.id)
            false
        }
    }

    companion object {
        const val MAX_ATTEMPTS = 10
        private val logger = LoggerFactory.getLogger(DeliverOutbox::class.java)
    }
}
