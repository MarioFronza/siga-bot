package br.com.mariofronza.sigabot.extraction

import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.usecases.academic.port.SigaLayoutChangedException
import br.com.mariofronza.sigabot.usecases.academic.port.SigaPeriodNotFoundException
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PeriodSelectionTest : FunSpec({

    val periods = mapOf("2025/2" to "31", "2026/1" to "40", "2024/1" to "12")

    test("picks the most recent period SIGA lists") {
        selectPeriods(periods, SyncScope.Latest) shouldBe listOf("40")
    }

    test("picks the requested period") {
        selectPeriods(periods, SyncScope.Period(AcademicPeriod(2025, 2))) shouldBe listOf("31")
    }

    test("picks every period") {
        selectPeriods(periods, SyncScope.Everything) shouldBe listOf("31", "40", "12")
    }

    test("reports a period SIGA does not list") {
        shouldThrow<SigaPeriodNotFoundException> { selectPeriods(periods, SyncScope.Period(AcademicPeriod(2030, 1))) }
    }

    test("reports a layout change when no listed period can be read") {
        shouldThrow<SigaLayoutChangedException> { selectPeriods(mapOf("Extensão 1" to "9"), SyncScope.Latest) }
    }
})
