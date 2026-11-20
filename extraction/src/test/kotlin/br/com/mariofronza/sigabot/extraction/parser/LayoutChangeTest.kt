package br.com.mariofronza.sigabot.extraction.parser

import br.com.mariofronza.sigabot.extraction.parser.dashboard.DashboardParser
import br.com.mariofronza.sigabot.extraction.parser.periods.PeriodsParser
import br.com.mariofronza.sigabot.extraction.parser.prelogin.CookieParser
import br.com.mariofronza.sigabot.extraction.parser.semesterresults.SemesterResultsParser
import br.com.mariofronza.sigabot.usecases.extraction.SigaLayoutChangedException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain

class LayoutChangeTest : FunSpec({

    val unknownPage = "<html><body><p>Página nova</p></body></html>"

    test("reports a layout change when the semester results link is missing") {
        shouldThrow<SigaLayoutChangedException> { DashboardParser().extractSemesterResultsUrl(unknownPage) }
            .message shouldContain "formPrincipal:linkAbre"
    }

    test("reports a layout change when the period list is missing") {
        shouldThrow<SigaLayoutChangedException> { PeriodsParser().extractPeriods(unknownPage) }
    }

    test("reports a layout change when the session form is missing") {
        shouldThrow<SigaLayoutChangedException> { CookieParser().extractAction(unknownPage) }
    }

    test("reports a layout change when the semester results are missing") {
        shouldThrow<SigaLayoutChangedException> { SemesterResultsParser().extractSemesterResults(unknownPage, "1") }
    }

    test("reports a layout change when the results table is missing") {
        val withoutTable = """
            <select id="lPeriodoLetivo"><option value="1">2026/1</option></select>
            <select id="lCurso"><option>-</option><option>ESO</option></select>
            <span id="resultado"></span>
        """

        shouldThrow<SigaLayoutChangedException> { SemesterResultsParser().extractSemesterResults(withoutTable, "1") }
    }
})
