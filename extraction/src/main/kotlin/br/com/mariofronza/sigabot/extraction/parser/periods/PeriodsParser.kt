package br.com.mariofronza.sigabot.extraction.parser.periods

import br.com.mariofronza.sigabot.extraction.parser.requireElementById
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class PeriodsParser {

    fun extractPeriods(responseContent: String): Map<String, String> {
        val document = Jsoup.parse(responseContent)
        val periodsSelect = document.requireElementById("lPeriodoLetivo")
        val periods = periodsSelect.getElementsByAttribute("value")
        val periodMap = mutableMapOf<String, String>()
        periods.map {
            periodMap[it.text()] = it.attr("value")
        }
        return periodMap.filter { it.key != "Extensão" }
    }
}
