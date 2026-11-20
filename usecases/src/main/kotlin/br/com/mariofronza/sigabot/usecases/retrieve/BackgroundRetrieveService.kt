package br.com.mariofronza.sigabot.usecases.retrieve

import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.retrieve.dto.RetrieveInput
import br.com.mariofronza.sigabot.usecases.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BackgroundRetrieveService(
    private val userService: UserService,
    private val retrieveService: RetrieveService,
) {

    fun retrieveDataOnBackground(year: Int, term: Int) {
        val users = userService.findAllActiveUsers()
        logger.info("Retrieving {}/{} for {} students", year, term, users.size)
        val failures = users.count { !retrieve(it, year, term) }
        logger.info("Retrieve finished, {} of {} students failed", failures, users.size)
    }

    private fun retrieve(user: User, year: Int, term: Int): Boolean = try {
        logger.debug("Retrieving user {}", user.id)
        retrieveService.retrieveForUser(
            retrieveInput = RetrieveInput(
                year = year,
                term = term,
            ),
            user = user
        )
        true
    } catch (e: Exception) {
        logger.warn("Retrieve failed for user {}", user.id, e)
        false
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BackgroundRetrieveService::class.java)
    }
}
