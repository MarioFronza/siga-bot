package br.com.mariofronza.sigabot.extraction.extractor

class ExtractorContext(
    var url: String = "",
    var viewState: String = "",
    var headerSessionId: String = "",
    var etts: String = "",
    var etp: String = "",
    var csrfToken: String = ""
)
