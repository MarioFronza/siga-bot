package br.com.mariofronza.sigabot.infrastructure.metrics

import br.com.mariofronza.sigabot.usecases.academic.port.SyncQueue
import br.com.mariofronza.sigabot.usecases.notification.DeliverOutbox
import br.com.mariofronza.sigabot.usecases.notification.port.Outbox
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class BacklogMetrics(
    private val queue: SyncQueue,
    private val outbox: Outbox,
    private val clock: Clock,
) : MeterBinder {

    override fun bindTo(registry: MeterRegistry) {
        Gauge.builder("siga.sync.queue.due") { queue.countDue(clock.instant()).toDouble() }.register(registry)
        Gauge.builder("siga.outbox.pending") { outbox.countPending(DeliverOutbox.MAX_ATTEMPTS).toDouble() }.register(registry)
    }
}
