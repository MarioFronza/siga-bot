package br.com.mariofronza.sigabot.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["br.com.mariofronza.sigabot"])
@EnableScheduling
class Boot

fun main(args: Array<String>) {
    requireEnvironment(System.getenv())
    runApplication<Boot>(*args)
}
