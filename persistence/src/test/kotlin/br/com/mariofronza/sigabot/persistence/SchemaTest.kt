package br.com.mariofronza.sigabot.persistence

import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.jooq.test.autoconfigure.JooqTest

@JooqTest
@ImportAutoConfiguration(FlywayAutoConfiguration::class)
class SchemaTest : FunSpec() {

    @Autowired
    lateinit var flyway: Flyway

    init {
        extension(SpringExtension())

        test("migrations apply") {
            flyway.info().pending().size shouldBe 0
        }
    }
}
