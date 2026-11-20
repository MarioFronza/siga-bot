package br.com.mariofronza.sigabot.usecases.retrieve

import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.user.UserService
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class BackgroundRetrieveServiceTest : FunSpec({

    fun student(id: Long) = User(
        id = UserId.generate(),
        isActive = true,
        email = Email.of("student$id@test.com"),
        passwordHash = "hash",
        roles = emptyList(),
        academicAccounts = emptyList(),
    )

    test("keeps retrieving the remaining students after one fails") {
        val (first, failing, last) = listOf(student(1), student(2), student(3))
        val userService: UserService = mockk()
        val retrieveService: RetrieveService = mockk(relaxed = true)
        every { userService.findAllActiveUsers() } returns listOf(first, failing, last)
        every { retrieveService.retrieveForUser(any(), failing) } throws IllegalStateException("SIGA rejected the login")

        BackgroundRetrieveService(userService, retrieveService).retrieveDataOnBackground(2026, 1)

        verify { retrieveService.retrieveForUser(any(), last) }
    }
})
