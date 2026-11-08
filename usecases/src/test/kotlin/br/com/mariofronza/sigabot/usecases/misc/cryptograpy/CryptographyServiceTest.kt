package br.com.mariofronza.sigabot.usecases.misc.cryptograpy

import br.com.mariofronza.sigabot.usecases.misc.config.ConfigService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk

class CryptographyServiceTest : FunSpec({

    val configService: ConfigService = mockk()
    val cryptographyService = CryptographyService(configService)

    beforeTest {
        every { configService.getRequiredString("secretKey") } returns generateSecretKey()
    }

    test("should encrypt data") {
        val stringToEncrypt = "nice string"
        val encryptedString = cryptographyService.encrypt(stringToEncrypt)

        encryptedString shouldNotBe stringToEncrypt
    }

    test("should decrypt data") {
        val stringToEncrypt = "nice string"
        val encryptedString = cryptographyService.encrypt(stringToEncrypt)

        val decryptedString = cryptographyService.decrypt(encryptedString)

        decryptedString shouldBe stringToEncrypt
    }

    test("should not repeat ciphertext for the same data") {
        cryptographyService.encrypt("nice string") shouldNotBe cryptographyService.encrypt("nice string")
    }

    test("should decrypt data stored by the legacy cbc scheme") {
        cryptographyService.decrypt("CXO8FLa/6rVbWlj32tJ+UA==") shouldBe "nice string"
    }
})

fun generateSecretKey(): String {
    return (1..32).joinToString("") { "A" }
}
