package br.com.mariofronza.sigabot.extraction.parser.prelogin

import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class CookieParser {

    fun extractAction(responseContent: String): String {
        val document = Jsoup.parse(responseContent)
        val form = document.getElementById("controleSessao")!!
        return form.attr("action")
    }
}
