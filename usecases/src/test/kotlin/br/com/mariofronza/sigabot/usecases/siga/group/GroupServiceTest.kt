package br.com.mariofronza.sigabot.usecases.siga.group

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.group.Group
import br.com.mariofronza.sigabot.entity.group.Period
import br.com.mariofronza.sigabot.usecases.siga.group.gateway.GroupInteractor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class GroupServiceTest : FunSpec({

    test("creates a new group when the same name exists only in another period") {
        val course = Course(id = 1, name = "Engenharia de Software")
        val firstTerm = Period(id = 1, year = "2026", term = 1)
        val secondTerm = Period(id = 2, year = "2026", term = 2)
        val stored = mutableListOf(Group(id = 1, name = "ESO-1", course = course, period = firstTerm))
        val groupInteractor: GroupInteractor = mockk()
        every { groupInteractor.find(any(), any(), any()) } answers {
            stored.firstOrNull { it.name == firstArg() && it.course == secondArg() && it.period == thirdArg() }
        }
        every { groupInteractor.save(any()) } answers { firstArg<Group>().copy(id = 2).also(stored::add) }

        val group = GroupService(groupInteractor).createGroup("ESO-1", course, secondTerm)

        group.period shouldBe secondTerm
    }
})
