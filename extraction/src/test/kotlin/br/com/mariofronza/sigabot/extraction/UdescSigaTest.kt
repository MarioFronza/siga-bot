package br.com.mariofronza.sigabot.extraction

import br.com.mariofronza.sigabot.entity.catalog.SubjectOffering
import br.com.mariofronza.sigabot.entity.enrollment.Attendance
import br.com.mariofronza.sigabot.entity.enrollment.Grade
import br.com.mariofronza.sigabot.entity.enrollment.SemesterOutcome
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.extraction.dto.SemesterResultOutput
import br.com.mariofronza.sigabot.extraction.dto.SemesterResultPeriod
import br.com.mariofronza.sigabot.extraction.dto.SemesterResultsExtractionOutput
import br.com.mariofronza.sigabot.usecases.academic.port.AcademicRecord
import br.com.mariofronza.sigabot.usecases.academic.port.AcademicSystem
import br.com.mariofronza.sigabot.usecases.academic.port.SigaLayoutChangedException
import br.com.mariofronza.sigabot.usecases.academic.port.SigaUnavailableException
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.io.IOException

class UdescSigaTest : FunSpec({

    val extraction: SemesterResultsExtraction = mockk()
    val siga = UdescSiga(extraction)
    val credentials = AcademicSystem.Credentials("00000000000", "secret")
    val period = AcademicPeriod(2026, 1)

    fun sigaShows(periodText: String, grade: Double = 8.5) {
        every { extraction.extract(any()) } returns SemesterResultsExtractionOutput(
            studentName = "Ana Souza",
            course = "Engenharia de Software",
            semesterResultsPeriod = listOf(
                SemesterResultPeriod(listOf(SemesterResultOutput("Banco de Dados", "ESO-1", grade, 72, 2, 95.0, "Aprovado")), periodText)
            ),
        )
    }

    test("translates the SIGA page into the domain") {
        sigaShows("2026/1")

        siga.fetch(credentials, SyncScope.Period(period)) shouldBe AcademicRecord(
            studentName = "Ana Souza",
            periods = listOf(period),
            results = listOf(
                AcademicRecord.Result(
                    SubjectOffering(InstitutionCode.UDESC, period, "Engenharia de Software", "ESO-1", "Banco de Dados", 72),
                    SemesterOutcome(Grade(8.5), 2, Attendance(95.0), "Aprovado"),
                )
            ),
        )
    }

    test("reports a layout change when SIGA shows something the domain rejects") {
        sigaShows("Extensão")

        shouldThrow<SigaLayoutChangedException> { siga.fetch(credentials, SyncScope.Period(period)) }
    }

    test("reports SIGA as unavailable when the connection fails") {
        every { extraction.extract(any()) } throws IOException("connection reset")

        shouldThrow<SigaUnavailableException> { siga.fetch(credentials, SyncScope.Everything) }
    }
})
