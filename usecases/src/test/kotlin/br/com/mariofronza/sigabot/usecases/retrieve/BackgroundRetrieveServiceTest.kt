package br.com.mariofronza.sigabot.usecases.retrieve

import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.misc.config.ConfigService
import br.com.mariofronza.sigabot.usecases.user.UserService
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class BackgroundRetrieveServiceTest : FunSpec({

    fun student(id: Long) = User(
        id = id,
        isActive = true,
        email = "student$id@test.com",
        passwordHash = "hash",
        roles = emptyList(),
        academicAccounts = emptyList(),
    )

    test("keeps retrieving the remaining students after one fails") {
        val (first, failing, last) = listOf(student(1), student(2), student(3))
        val userService: UserService = mockk()
        val retrieveService: RetrieveService = mockk(relaxed = true)
        val configService: ConfigService = mockk()
        every { configService.getRequiredInt(any()) } returns 2026
        every { userService.findAllActiveUsers() } returns listOf(first, failing, last)
        every { retrieveService.retrieveForUser(any(), failing) } throws IllegalStateException("SIGA rejected the login")

        BackgroundRetrieveService(userService, retrieveService, configService).retrieveDataOnBackground()

        verify { retrieveService.retrieveForUser(any(), last) }
    }
})
