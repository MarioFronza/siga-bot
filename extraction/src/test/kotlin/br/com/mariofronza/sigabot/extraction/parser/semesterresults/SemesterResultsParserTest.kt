package br.com.mariofronza.sigabot.extraction.parser.semesterresults

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class SemesterResultsParserTest : FunSpec({

    fun page(vararg rows: String) = """
        <select id="lPeriodoLetivo"><option value="1">2026/2</option></select>
        <select id="lCurso"><option>-</option><option>ESO</option></select>
        <span id="resultado"><center><table class="delimitador"><tbody>
            <tr><td>Disciplina</td><td>Turma</td><td>Nota</td><td></td><td></td><td>CH</td><td></td><td>Faltas</td><td>Freq</td><td>Situação</td></tr>
            ${rows.joinToString("")}
        </tbody></table></center></span>
    """

    fun row(grade: String, attendance: String) =
        "<tr><td>Banco de Dados</td><td>BAN01</td><td>$grade</td><td></td><td></td><td>72</td><td></td><td>2</td><td>$attendance</td><td>Cursando</td></tr>"

    test("reads the grade and attendance of a finished subject") {
        val result = SemesterResultsParser().extractSemesterResults(page(row("9,2", "97,2")), "1").semesterResults.single()

        result.finalGrade shouldBe 9.2
        result.attendancePercentage shouldBe 97.2
    }

    test("leaves the grade and attendance empty while the subject is in progress") {
        val result = SemesterResultsParser().extractSemesterResults(page(row("", "")), "1").semesterResults.single()

        result.finalGrade.shouldBeNull()
        result.attendancePercentage.shouldBeNull()
    }
})
