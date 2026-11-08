package br.com.mariofronza.sigabot.extraction.parser.dashboard

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DashboardParserTest : FunSpec({

    val dashboard = """
        <ul>
          <li id="formMenu:menu_3_3" role="menuitem"><a href="/rematricula"><span>Rematrícula/Ajuste</span></a></li>
          <li id="formMenu:menu_4_3" role="menuitem"><a href="/resultados"><span>Resultados Semestrais</span></a></li>
        </ul>
        <form id="formMenu">
          <script id="formMenu:j_idt111" type="text/javascript">PrimeFaces.cw("Menu")</script>
          <img id="formMenu:j_idt114" class="avatar-left-menu" />
          <label id="formMenu:j_idt116" class="ui-outputlabel ui-widget Fs16 FontBold">Maria da Silva</label>
          <label id="formMenu:j_idt118" class="ui-outputlabel ui-widget Fs12 FontBold">ESO08 - Curso</label>
        </form>
    """

    test("finds the semester results link by its menu label") {
        DashboardParser().extractDashboardSemesterResultsUrl(dashboard) shouldBe "/resultados"
    }

    test("reads the student name next to the menu avatar") {
        DashboardParser().extractStudentName(dashboard) shouldBe "Maria da Silva"
    }
})
