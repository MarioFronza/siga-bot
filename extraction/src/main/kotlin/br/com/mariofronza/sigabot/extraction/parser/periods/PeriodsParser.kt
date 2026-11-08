package br.com.mariofronza.sigabot.extraction.parser.periods

import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class PeriodsParser {

    fun extractPeriods(responseContent: String): Map<String, String> {
        val document = Jsoup.parse(responseContent)
        val periodsSelect = document.getElementById("lPeriodoLetivo")!!
        val periods = periodsSelect.getElementsByAttribute("value")
        val periodMap = mutableMapOf<String, String>()
        periods.map {
            periodMap[it.text()] = it.attr("value")
        }
        return periodMap.filter { it.key != "Extensão" }
    }
}
