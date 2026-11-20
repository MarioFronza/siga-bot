package br.com.mariofronza.sigabot.extraction

import br.com.mariofronza.sigabot.usecases.academic.port.SigaLayoutChangedException
import br.com.mariofronza.sigabot.usecases.academic.port.SigaPeriodNotFoundException
import br.com.mariofronza.sigabot.usecases.academic.port.SigaUnavailableException
import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse
import br.com.mariofronza.sigabot.extraction.connector.Connector
import br.com.mariofronza.sigabot.extraction.connector.RetrofitSigaConnector
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import br.com.mariofronza.sigabot.extraction.extractor.dashboard.DashboardExtractor
import br.com.mariofronza.sigabot.extraction.extractor.periods.PeriodsExtractor
import br.com.mariofronza.sigabot.extraction.extractor.prelogin.CookieExtractor
import br.com.mariofronza.sigabot.extraction.extractor.semesterresults.DashboardSemesterResultsExtractor
import br.com.mariofronza.sigabot.extraction.extractor.semesterresults.SemesterResultsExtractor
import br.com.mariofronza.sigabot.extraction.parser.dashboard.DashboardParser
import br.com.mariofronza.sigabot.extraction.parser.periods.PeriodsParser
import br.com.mariofronza.sigabot.extraction.parser.prelogin.CookieParser
import br.com.mariofronza.sigabot.extraction.parser.semesterresults.SemesterResultsParser
import br.com.mariofronza.sigabot.extraction.dto.SemesterResultsExtractionInput
import br.com.mariofronza.sigabot.extraction.dto.SemesterResultsExtractionOutput
import org.springframework.stereotype.Component

@Component
class SemesterResultsExtraction(
    private val cookieExtractor: CookieExtractor,
    private val dashboardExtractor: DashboardExtractor,
    private val dashboardSemesterResultsExtractor: DashboardSemesterResultsExtractor,
    private val periodsExtractor: PeriodsExtractor,
    private val semesterResultsExtractor: SemesterResultsExtractor,
    private val cookieParser: CookieParser,
    private val dashboardParser: DashboardParser,
    private val periodsParser: PeriodsParser,
    private val semesterResultsParser: SemesterResultsParser,
) {

    private val retrofitSigaConnection: RetrofitSigaConnector

    init {
        val connector = Connector()
        retrofitSigaConnection = connector.createConnection()
    }

    fun extract(
        extractSemesterResultsInput: SemesterResultsExtractionInput,
        completeExtraction: Boolean
    ): SemesterResultsExtractionOutput {
        val extractorContext = ExtractorContext()
        extractCookie(extractorContext)
        val studentName = extractDashboard(extractorContext, extractSemesterResultsInput)
        extractSemesterResultDashboard(extractorContext)
        val periods = extractPeriods(extractorContext)
        val periodsIdentified = if (completeExtraction) {
            periods.map { it.value }
        } else {
            getPeriodSigaIdentified(
                year = extractSemesterResultsInput.period.year,
                term = extractSemesterResultsInput.period.term,
                periods
            )
        }

        // TODO use coroutines here
        val semesterResultsOutput = periodsIdentified.map {
            extractSemesterResults(extractorContext, it)
        }

        return SemesterResultsExtractionOutput(
            studentName = studentName,
            course = semesterResultsOutput.firstOrNull()?.course ?: throw SigaLayoutChangedException("SIGA listed no periods"),
            semesterResultsPeriod = semesterResultsOutput.map { it.toSemesterResultsPeriod() }
        )
    }

    private fun extractCookie(extractorContext: ExtractorContext) {
        extractorContext.url = when (val response = cookieExtractor.execute(retrofitSigaConnection, extractorContext)) {
            is StepResponse.StepSuccess<*> -> {
                cookieParser.extractAction(response.payload.toString())
            }
            else -> throw SigaUnavailableException("SIGA did not open a session")
        }
    }

    private fun extractDashboard(
        extractorContext: ExtractorContext,
        extractSemesterResultsInput: SemesterResultsExtractionInput): String {
        val payload = when (val response =
            dashboardExtractor.execute(retrofitSigaConnection, extractorContext, extractSemesterResultsInput)) {
            is StepResponse.StepSuccess<*> -> {
                response.payload.toString()
            }
            else -> throw SigaUnavailableException("SIGA did not answer the login")
        }
        extractorContext.url = dashboardParser.extractDashboardSemesterResultsUrl(payload)
        return dashboardParser.extractStudentName(payload)
    }

    private fun extractSemesterResultDashboard(extractorContext: ExtractorContext) {
        extractorContext.url =
            when (val response =
                dashboardSemesterResultsExtractor.execute(retrofitSigaConnection, extractorContext)) {
                is StepResponse.StepSuccess<*> -> {
                    dashboardParser.extractSemesterResultsUrl(response.payload.toString())
                }
                else -> throw SigaUnavailableException("SIGA did not open the semester results")
            }
    }

    private fun extractPeriods(extractorContext: ExtractorContext): Map<String, String> {
        return when (val response =
            periodsExtractor.execute(retrofitSigaConnection, extractorContext)) {
            is StepResponse.StepSuccess<*> -> {
                periodsParser.extractPeriods(response.payload.toString())
            }
            else -> throw SigaUnavailableException("SIGA did not list the periods")
        }
    }

    private fun extractSemesterResults(
        extractorContext: ExtractorContext,
        periodIdentified: String
    ): SemesterResultsParser.SemesterResultsResponse {
        return when (val response =
            semesterResultsExtractor.execute(retrofitSigaConnection, extractorContext, periodIdentified)) {
            is StepResponse.StepSuccess<*> -> {
                semesterResultsParser.extractSemesterResults(response.payload.toString(), periodIdentified)
            }
            else -> throw SigaUnavailableException("SIGA did not answer the period $periodIdentified")
        }
    }

    private fun getPeriodSigaIdentified(year: String, term: Int, periods: Map<String, String>): List<String> {
        return listOf(periods["$year/$term"] ?: throw SigaPeriodNotFoundException("$year/$term"))
    }
}
