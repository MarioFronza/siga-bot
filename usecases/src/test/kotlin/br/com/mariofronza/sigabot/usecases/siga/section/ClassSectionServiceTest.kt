package br.com.mariofronza.sigabot.usecases.siga.section

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.term.AcademicTerm
import br.com.mariofronza.sigabot.usecases.siga.section.gateway.ClassSectionInteractor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class ClassSectionServiceTest : FunSpec({

    test("creates a new group when the same name exists only in another period") {
        val course = Course(id = 1, name = "Engenharia de Software")
        val firstTerm = AcademicTerm(id = 1, year = "2026", term = 1)
        val secondTerm = AcademicTerm(id = 2, year = "2026", term = 2)
        val stored = mutableListOf(ClassSection(id = 1, name = "ESO-1", course = course, period = firstTerm))
        val classSectionInteractor: ClassSectionInteractor = mockk()
        every { classSectionInteractor.find(any(), any(), any()) } answers {
            stored.firstOrNull { it.name == firstArg() && it.course == secondArg() && it.period == thirdArg() }
        }
        every { classSectionInteractor.save(any()) } answers { firstArg<ClassSection>().copy(id = 2).also(stored::add) }

        val group = ClassSectionService(classSectionInteractor).createGroup("ESO-1", course, secondTerm)

        group.period shouldBe secondTerm
    }
})
