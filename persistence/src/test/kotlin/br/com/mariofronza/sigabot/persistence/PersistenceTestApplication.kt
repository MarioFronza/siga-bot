package br.com.mariofronza.sigabot.persistence

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer

@SpringBootApplication
class PersistenceTestApplication {

    @Bean
    @ServiceConnection
    fun postgres() = PostgreSQLContainer("postgres:18-alpine")
}
