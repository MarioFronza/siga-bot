package br.com.mariofronza.sigabot.extraction.apisteps.semesterresults

import br.com.mariofronza.sigabot.extraction.apisteps.Step
import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse
import br.com.mariofronza.sigabot.extraction.connector.RetrofitSigaConnector
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import org.springframework.stereotype.Component

@Component
class SemesterResultsWithQueryStep : Step() {

    fun execute(
        connection: RetrofitSigaConnector,
        extractorContext: ExtractorContext,
        periodIdentified: String
    ): StepResponse {
        val headers = mapOf(
            "Cookie" to "${extractorContext.headerSessionId}; ECS=S; ${extractorContext.etts}"
        )

        val body = mapOf(
            "lookupCorrentePlc" to "",
            "navSetaFocoPlc" to "",
            "modoPlc" to "consultaPlc",
            "detCorrPlc" to "",
            "indExcDetPlc" to "",
            "evento" to "Executar Consulta",
            "lPeriodoLetivo" to periodIdentified,
            "lCurso" to "",
            "estadoJanelaimportacao" to "fecha",
            "caracterSep" to "",
            "idConsulta" to "2",
            "exibecomp" to "N",
            "exibeComboPerLet" to "S",
            "nomeArquivoPdf" to ""
        )

        return connection.semesterResultsWithPeriod(headers, body)
            .execute()
            .getBody()
    }
}
