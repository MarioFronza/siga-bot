package br.com.mariofronza.sigabot.extraction.parser.prelogin

import br.com.mariofronza.sigabot.extraction.parser.requireElementById
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class CookieParser {

    fun extractAction(responseContent: String): String {
        val document = Jsoup.parse(responseContent)
        val form = document.requireElementById("controleSessao")
        return form.attr("action")
    }
}
