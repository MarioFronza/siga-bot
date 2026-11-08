package br.com.mariofronza.sigabot.http.controller

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/health")
class HealthController {

    @GetMapping
    fun show(): Boolean {
        return true
    }
}
