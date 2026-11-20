package br.com.mariofronza.sigabot.infrastructure.crypto

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class AesSecretCipherTest : FunSpec({

    val cipher = AesSecretCipher("A".repeat(32))

    test("should encrypt data") {
        val stringToEncrypt = "nice string"
        val encryptedString = cipher.encrypt(stringToEncrypt)

        encryptedString shouldNotBe stringToEncrypt
    }

    test("should decrypt data") {
        val stringToEncrypt = "nice string"
        val encryptedString = cipher.encrypt(stringToEncrypt)

        val decryptedString = cipher.decrypt(encryptedString)

        decryptedString shouldBe stringToEncrypt
    }

    test("should not repeat ciphertext for the same data") {
        cipher.encrypt("nice string") shouldNotBe cipher.encrypt("nice string")
    }

    test("should derive the same digest for the same data") {
        cipher.digest("00000000000") shouldBe cipher.digest("00000000000")
    }

    test("should derive a different digest under a different key") {
        AesSecretCipher("B".repeat(32)).digest("00000000000") shouldNotBe cipher.digest("00000000000")
    }

    test("should decrypt data stored by the legacy cbc scheme") {
        cipher.decrypt("CXO8FLa/6rVbWlj32tJ+UA==") shouldBe "nice string"
    }
})
