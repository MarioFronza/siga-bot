package br.com.mariofronza.sigabot.infrastructure.scheduling

import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.usecases.academic.RunDueSyncs
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import br.com.mariofronza.sigabot.usecases.notification.DeliverOutbox
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RetrieveScheduler(
    private val runDueSyncs: RunDueSyncs,
    private val deliverOutbox: DeliverOutbox,
    @param:Value("\${siga.year:#{null}}")
    year: Int?,
    @param:Value("\${siga.term:#{null}}")
    term: Int?,
) {

    private val scope: SyncScope = periodOverride(year, term)?.let(SyncScope::Period) ?: SyncScope.Latest

    @Scheduled(fixedDelayString = "\${sync.poll-delay:PT30S}")
    fun runDueSyncs() {
        runDueSyncs(scope)
    }

    @Scheduled(fixedDelayString = "\${outbox.poll-delay:PT10S}")
    fun deliverOutbox() {
        deliverOutbox.invoke()
    }

    private fun periodOverride(year: Int?, term: Int?): AcademicPeriod? =
        if (year != null && term != null) AcademicPeriod(year, term) else null
}
