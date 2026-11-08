package br.com.mariofronza.sigabot.usecases.retrieve

import br.com.mariofronza.sigabot.usecases.misc.config.ConfigService
import br.com.mariofronza.sigabot.usecases.retrieve.dto.RetrieveInput
import br.com.mariofronza.sigabot.usecases.user.UserService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class BackgroundRetrieveService(
    private val userService: UserService,
    private val retrieveService: RetrieveService,
    configService: ConfigService,
) {

    private val sigaYear = configService.getRequiredString("siga.year")
    private val sigaTerm = configService.getRequiredInt("siga.term")

    @Scheduled(cron = "0 0 0/1 * * ?")
    fun retrieveDataOnBackground() {
        val users = userService.findAllActiveUsers()
        println("Retrieve background service")
        users.forEach { user ->
            println("Retrieve for ${user.username}")
            user.groups
            retrieveService.retrieveForUser(
                retrieveInput = RetrieveInput(
                    userId = user.id!!,
                    year = sigaYear,
                    term = sigaTerm,
                ),
                user = user
            )
        }
    }
}
