package br.com.mariofronza.sigabot.extraction

import br.com.mariofronza.sigabot.entity.catalog.SubjectOffering
import br.com.mariofronza.sigabot.entity.enrollment.Attendance
import br.com.mariofronza.sigabot.entity.enrollment.Grade
import br.com.mariofronza.sigabot.entity.enrollment.SemesterOutcome
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.extraction.dto.PeriodInput
import br.com.mariofronza.sigabot.extraction.dto.SemesterResultOutput
import br.com.mariofronza.sigabot.extraction.dto.SemesterResultsExtractionInput
import br.com.mariofronza.sigabot.extraction.dto.SemesterResultsExtractionOutput
import br.com.mariofronza.sigabot.extraction.dto.SigaCredentialInput
import br.com.mariofronza.sigabot.usecases.academic.port.AcademicRecord
import br.com.mariofronza.sigabot.usecases.academic.port.AcademicSystem
import br.com.mariofronza.sigabot.usecases.academic.port.SigaLayoutChangedException
import br.com.mariofronza.sigabot.usecases.academic.port.SigaUnavailableException
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class UdescSiga(
    private val extraction: SemesterResultsExtraction,
) : AcademicSystem {

    override val institution = InstitutionCode.UDESC

    override fun fetch(credentials: AcademicSystem.Credentials, scope: SyncScope): AcademicRecord {
        val output = try {
            extraction.extract(inputOf(credentials, scope), completeExtraction = scope == SyncScope.Everything)
        } catch (e: IOException) {
            throw SigaUnavailableException("SIGA did not answer", e)
        }
        return try {
            recordOf(output)
        } catch (e: IllegalArgumentException) {
            throw SigaLayoutChangedException("SIGA showed data the domain rejects: ${e.message}")
        }
    }

    private fun inputOf(credentials: AcademicSystem.Credentials, scope: SyncScope): SemesterResultsExtractionInput {
        val period = when (scope) {
            is SyncScope.Period -> PeriodInput(year = scope.period.year.toString(), term = scope.period.term)
            SyncScope.Everything -> PeriodInput(year = "0", term = 0)
        }
        return SemesterResultsExtractionInput(SigaCredentialInput(credentials.login, credentials.password), period)
    }

    private fun recordOf(output: SemesterResultsExtractionOutput): AcademicRecord {
        val periods = output.semesterResultsPeriod.map { AcademicPeriod.parse(it.period) to it.semesterResults }
        return AcademicRecord(
            studentName = output.studentName,
            periods = periods.map { (period, _) -> period },
            results = periods.flatMap { (period, results) -> results.map { resultOf(period, output.course, it) } },
        )
    }

    private fun resultOf(period: AcademicPeriod, course: String, result: SemesterResultOutput) = AcademicRecord.Result(
        offering = SubjectOffering(institution, period, course, result.groupName, result.subjectName, result.courseLoad),
        outcome = SemesterOutcome(Grade(result.finalGrade), result.absencesCount, Attendance(result.attendancePercentage), result.result),
    )
}
