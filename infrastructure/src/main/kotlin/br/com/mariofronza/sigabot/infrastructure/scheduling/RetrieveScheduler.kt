package br.com.mariofronza.sigabot.infrastructure.scheduling

import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.usecases.academic.SyncAllStudents
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RetrieveScheduler(
    private val syncAllStudents: SyncAllStudents,
    @param:Value("\${siga.year:#{null}}")
    year: Int?,
    @param:Value("\${siga.term:#{null}}")
    term: Int?,
) {

    private val scope: SyncScope = periodOverride(year, term)?.let(SyncScope::Period) ?: SyncScope.Latest

    @Scheduled(cron = "0 0 0/1 * * ?")
    fun retrieve() {
        syncAllStudents(scope)
    }

    private fun periodOverride(year: Int?, term: Int?): AcademicPeriod? =
        if (year != null && term != null) AcademicPeriod(year, term) else null
}
