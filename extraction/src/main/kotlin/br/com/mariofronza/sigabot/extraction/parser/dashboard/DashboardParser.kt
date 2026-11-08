package br.com.mariofronza.sigabot.extraction.parser.dashboard

import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class DashboardParser {

    fun extractDashboardSemesterResultsUrl(responseContent: String): String = Jsoup.parse(responseContent)
        .select("li[id^=formMenu:menu_] > a")
        .first { it.text().startsWith(SEMESTER_RESULTS_MENU) }
        .attr("href")

    fun extractSemesterResultsUrl(responseContent: String): String {
        val document = Jsoup.parse(responseContent)
        val input = document.getElementById("formPrincipal:linkAbre")!!
        return input.attr("value")
    }

    fun extractStudentName(responseContent: String): String =
        Jsoup.parse(responseContent).expectFirst("#formMenu label.Fs16").text()

    companion object {
        const val SEMESTER_RESULTS_MENU = "Resultados Semestrais"
    }
}
