package br.com.mariofronza.sigabot.extraction.parser

import br.com.mariofronza.sigabot.usecases.extraction.SigaLayoutChangedException
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

internal fun Document.requireElementById(id: String): Element =
    getElementById(id) ?: throw SigaLayoutChangedException("SIGA page has no element #$id")
