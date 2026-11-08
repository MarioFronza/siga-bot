package br.com.mariofronza.sigabot.extraction

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
import br.com.mariofronza.sigabot.usecases.extraction.semester.SemesterResultsExtraction
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SemesterResultsExtractionInput
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SemesterResultsExtractionOutput
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
) : SemesterResultsExtraction {

    private val retrofitSigaConnection: RetrofitSigaConnector

    init {
        val connector = Connector()
        retrofitSigaConnection = connector.createConnection()
    }

    override fun extract(
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
            course = semesterResultsOutput.first().course,
            semesterResultsPeriod = semesterResultsOutput.map { it.toSemesterResultsPeriod() }
        )
    }

    private fun extractCookie(extractorContext: ExtractorContext) {
        extractorContext.url = when (val response = cookieExtractor.execute(retrofitSigaConnection, extractorContext)) {
            is StepResponse.StepSuccess<*> -> {
                cookieParser.extractAction(response.payload.toString())
            }
            else -> throw Exception("Cookie parser unexpected error")
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
            else -> throw Exception("Dashboard parser unexpected error")
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
                else -> throw Exception("Semester results parser unexpected error")
            }
    }

    private fun extractPeriods(extractorContext: ExtractorContext): Map<String, String> {
        return when (val response =
            periodsExtractor.execute(retrofitSigaConnection, extractorContext)) {
            is StepResponse.StepSuccess<*> -> {
                periodsParser.extractPeriods(response.payload.toString())
            }
            else -> throw Exception("Periods parser unexpected error")
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
            else -> throw Exception("Semester results parser unexpected error")
        }
    }

    private fun getPeriodSigaIdentified(year: String, term: Int, periods: Map<String, String>): List<String> {
        return listOf(periods["$year/$term"] ?: throw Exception("Invalid Period"))
    }
}
