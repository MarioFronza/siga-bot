package br.com.mariofronza.sigabot.extraction

import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.usecases.academic.port.SigaLayoutChangedException
import br.com.mariofronza.sigabot.usecases.academic.port.SigaPeriodNotFoundException
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope

internal fun selectPeriods(periods: Map<String, String>, scope: SyncScope): List<String> = when (scope) {
    SyncScope.Everything -> periods.values.toList()
    is SyncScope.Period -> listOf(periods[scope.period.toString()] ?: throw SigaPeriodNotFoundException(scope.period.toString()))
    SyncScope.Latest -> listOf(latest(periods))
}

private fun latest(periods: Map<String, String>): String =
    periods.mapNotNull { (label, id) -> runCatching { AcademicPeriod.parse(label) }.getOrNull()?.let { it to id } }
        .maxByOrNull { (period, _) -> period }
        ?.second
        ?: throw SigaLayoutChangedException("SIGA listed no period in the year/term format")
