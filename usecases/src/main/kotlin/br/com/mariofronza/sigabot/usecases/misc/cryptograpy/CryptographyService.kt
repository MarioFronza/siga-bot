package br.com.mariofronza.sigabot.usecases.misc.cryptograpy

import br.com.mariofronza.sigabot.usecases.misc.config.ConfigService
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
class CryptographyService(
    private val configService: ConfigService
) {

    private val encoder = Base64.getEncoder()
    private val decoder = Base64.getDecoder()
    private val random = SecureRandom()

    fun encrypt(str: String): String {
        val iv = ByteArray(IV_LENGTH).also(random::nextBytes)
        val encrypted = cipher(Cipher.ENCRYPT_MODE, iv).doFinal(str.toByteArray(Charsets.UTF_8))
        return VERSION_PREFIX + encoder.encodeToString(iv + encrypted)
    }

    fun decrypt(str: String): String =
        if (str.startsWith(VERSION_PREFIX)) decryptGcm(str.removePrefix(VERSION_PREFIX)) else decryptLegacy(str)

    private fun decryptGcm(str: String): String {
        val bytes = decoder.decode(str)
        val iv = bytes.copyOfRange(0, IV_LENGTH)
        return String(cipher(Cipher.DECRYPT_MODE, iv).doFinal(bytes, IV_LENGTH, bytes.size - IV_LENGTH), Charsets.UTF_8)
    }

    // Values written before v2 used AES-CBC with the key prefix as a fixed IV. Read-only support.
    private fun decryptLegacy(str: String): String {
        val key = secretKey()
        val iv = IvParameterSpec(key.encoded.copyOfRange(0, 16))
        val cipher = Cipher.getInstance(LEGACY_TRANSFORMATION).apply { init(Cipher.DECRYPT_MODE, key, iv) }
        return String(cipher.doFinal(decoder.decode(str)), Charsets.UTF_8)
    }

    private fun cipher(opmode: Int, iv: ByteArray): Cipher =
        Cipher.getInstance(TRANSFORMATION).apply { init(opmode, secretKey(), GCMParameterSpec(TAG_BITS, iv)) }

    private fun secretKey(): SecretKeySpec {
        val secretKey = configService.getRequiredString("secretKey")
        if (secretKey.length != 32) throw RuntimeException("SecretKey length is not 32 chars")
        return SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), ALGORITHM)
    }

    companion object {
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val LEGACY_TRANSFORMATION = "AES/CBC/PKCS5Padding"
        const val ALGORITHM = "AES"
        const val VERSION_PREFIX = "v2:"
        const val IV_LENGTH = 12
        const val TAG_BITS = 128
    }
}
