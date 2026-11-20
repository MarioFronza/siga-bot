package br.com.mariofronza.sigabot.usecases.siga.section

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.usecases.siga.section.gateway.ClassSectionInteractor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class ClassSectionServiceTest : FunSpec({

    test("creates a new class section when the same code exists only in another academic term") {
        val udesc = Institution(id = 1, code = "udesc", name = "UDESC", gradeScaleMax = 10.0)
        val course = Course(id = 1, institution = udesc, name = "Engenharia de Software")
        val firstTerm = AcademicTerm(id = 1, institution = udesc, year = 2026, term = 1)
        val secondTerm = AcademicTerm(id = 2, institution = udesc, year = 2026, term = 2)
        val stored = mutableListOf(ClassSection(id = 1, code = "ESO-1", course = course, academicTerm = firstTerm))
        val classSectionInteractor: ClassSectionInteractor = mockk()
        every { classSectionInteractor.find(any(), any(), any()) } answers {
            stored.firstOrNull { it.code == firstArg() && it.course == secondArg() && it.academicTerm == thirdArg() }
        }
        every { classSectionInteractor.save(any()) } answers { firstArg<ClassSection>().copy(id = 2).also(stored::add) }

        val classSection = ClassSectionService(classSectionInteractor).findOrCreate("ESO-1", course, secondTerm)

        classSection.academicTerm shouldBe secondTerm
    }
})
