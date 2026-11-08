package br.com.mariofronza.sigabot.http.controller.retrieve

import br.com.mariofronza.sigabot.usecases.retrieve.RetrieveService
import br.com.mariofronza.sigabot.usecases.retrieve.dto.RetrieveInput
import br.com.mariofronza.sigabot.usecases.retrieve.dto.RetrieveOutput
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/retrieve")
class RetrieveController(
    private val retrieveService: RetrieveService
) {

    @GetMapping
    fun retrieve(
        @RequestParam year: String,
        @RequestParam term: String
    ): RetrieveOutput {
        return retrieveService.retrieve(
            RetrieveInput(
                year = year,
                term = term.toInt()
            )
        )
    }

    @GetMapping("/full")
    fun fullRetrieve(): RetrieveOutput {
        return retrieveService.fullRetrieve(RetrieveInput())
    }
}
