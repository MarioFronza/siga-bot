package br.com.mariofronza.sigabot.infrastructure.scheduling

import br.com.mariofronza.sigabot.usecases.retrieve.BackgroundRetrieveService
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RetrieveScheduler(
    private val backgroundRetrieveService: BackgroundRetrieveService,
    @param:Value("\${siga.year}")
    private val year: Int,
    @param:Value("\${siga.term}")
    private val term: Int,
) {

    @Scheduled(cron = "0 0 0/1 * * ?")
    fun retrieve() {
        backgroundRetrieveService.retrieveDataOnBackground(year, term)
    }
}
