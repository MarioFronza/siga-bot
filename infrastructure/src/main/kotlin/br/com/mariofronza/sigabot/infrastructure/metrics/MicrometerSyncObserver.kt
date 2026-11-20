package br.com.mariofronza.sigabot.infrastructure.metrics

import br.com.mariofronza.sigabot.usecases.academic.port.SyncObserver
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class MicrometerSyncObserver(
    private val registry: MeterRegistry,
) : SyncObserver {

    override fun synced(duration: Duration) {
        registry.timer("siga.sync.duration").record(duration)
    }

    override fun failed(error: Throwable) {
        registry.counter("siga.sync.failures", "reason", error.javaClass.simpleName).increment()
    }
}
