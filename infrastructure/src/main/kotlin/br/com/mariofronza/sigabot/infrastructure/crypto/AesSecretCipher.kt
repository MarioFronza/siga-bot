package br.com.mariofronza.sigabot.infrastructure.crypto

import br.com.mariofronza.sigabot.usecases.identity.port.SecretCipher
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class AesSecretCipher(
    @param:Value("\${secretKey}")
    secretKey: String,
) : SecretCipher {

    private val key: SecretKeySpec = run {
        require(secretKey.length == 32) { "SECRETKEY must have 32 chars" }
        SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), ALGORITHM)
    }

    private val encoder = Base64.getEncoder()
    private val decoder = Base64.getDecoder()
    private val random = SecureRandom()

    override fun encrypt(plainText: String): String {
        val iv = ByteArray(IV_LENGTH).also(random::nextBytes)
        val encrypted = cipher(Cipher.ENCRYPT_MODE, iv).doFinal(plainText.toByteArray(Charsets.UTF_8))
        return VERSION_PREFIX + encoder.encodeToString(iv + encrypted)
    }

    override fun decrypt(cipherText: String): String =
        if (cipherText.startsWith(VERSION_PREFIX)) decryptGcm(cipherText.removePrefix(VERSION_PREFIX)) else decryptLegacy(cipherText)

    override fun digest(value: String): ByteArray =
        hmac(hmac(key.encoded, DIGEST_KEY_LABEL.toByteArray()), value.toByteArray(Charsets.UTF_8))

    private fun hmac(key: ByteArray, data: ByteArray): ByteArray =
        Mac.getInstance(HMAC).apply { init(SecretKeySpec(key, HMAC)) }.doFinal(data)

    private fun decryptGcm(str: String): String {
        val bytes = decoder.decode(str)
        val iv = bytes.copyOfRange(0, IV_LENGTH)
        return String(cipher(Cipher.DECRYPT_MODE, iv).doFinal(bytes, IV_LENGTH, bytes.size - IV_LENGTH), Charsets.UTF_8)
    }

    // Values written before v2 used AES-CBC with the key prefix as a fixed IV. Read-only support.
    private fun decryptLegacy(str: String): String {
        val iv = IvParameterSpec(key.encoded.copyOfRange(0, 16))
        val cipher = Cipher.getInstance(LEGACY_TRANSFORMATION).apply { init(Cipher.DECRYPT_MODE, key, iv) }
        return String(cipher.doFinal(decoder.decode(str)), Charsets.UTF_8)
    }

    private fun cipher(opmode: Int, iv: ByteArray): Cipher =
        Cipher.getInstance(TRANSFORMATION).apply { init(opmode, key, GCMParameterSpec(TAG_BITS, iv)) }

    companion object {
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val LEGACY_TRANSFORMATION = "AES/CBC/PKCS5Padding"
        const val ALGORITHM = "AES"
        const val VERSION_PREFIX = "v2:"
        const val IV_LENGTH = 12
        const val TAG_BITS = 128
        const val HMAC = "HmacSHA256"
        const val DIGEST_KEY_LABEL = "digest"
    }
}
