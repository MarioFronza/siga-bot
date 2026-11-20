package br.com.mariofronza.sigabot.persistence

import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SchemaTest : FunSpec() {

    @Autowired
    lateinit var flyway: Flyway

    init {
        extension(SpringExtension())

        test("migrations apply and match the JPA mappings") {
            flyway.info().pending().size shouldBe 0
        }
    }
}
