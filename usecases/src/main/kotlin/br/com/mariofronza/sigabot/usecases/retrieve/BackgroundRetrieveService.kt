package br.com.mariofronza.sigabot.usecases.retrieve

import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.misc.config.ConfigService
import br.com.mariofronza.sigabot.usecases.retrieve.dto.RetrieveInput
import br.com.mariofronza.sigabot.usecases.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class BackgroundRetrieveService(
    private val userService: UserService,
    private val retrieveService: RetrieveService,
    configService: ConfigService,
) {

    private val sigaYear = configService.getRequiredInt("siga.year")
    private val sigaTerm = configService.getRequiredInt("siga.term")

    @Scheduled(cron = "0 0 0/1 * * ?")
    fun retrieveDataOnBackground() {
        val users = userService.findAllActiveUsers()
        logger.info("Retrieving {}/{} for {} students", sigaYear, sigaTerm, users.size)
        val failures = users.count { !retrieve(it) }
        logger.info("Retrieve finished, {} of {} students failed", failures, users.size)
    }

    private fun retrieve(user: User): Boolean = try {
        logger.debug("Retrieving user {}", user.externalId)
        retrieveService.retrieveForUser(
            retrieveInput = RetrieveInput(
                year = sigaYear,
                term = sigaTerm,
            ),
            user = user
        )
        true
    } catch (e: Exception) {
        logger.warn("Retrieve failed for user {}", user.externalId, e)
        false
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BackgroundRetrieveService::class.java)
    }
}
