package br.com.mariofronza.sigabot.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["br.com.mariofronza.sigabot"])
@EnableScheduling
@EnableJpaRepositories(basePackages = ["br.com.mariofronza.sigabot.persistence"])
@EntityScan(basePackages = ["br.com.mariofronza.sigabot.persistence"])
class Boot

fun main(args: Array<String>) {
    requireEnvironment(System.getenv())
    runApplication<Boot>(*args)
}
