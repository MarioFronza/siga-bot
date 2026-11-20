package br.com.mariofronza.sigabot.usecases.identity.port

interface SecretCipher {

    fun encrypt(plainText: String): String

    fun decrypt(cipherText: String): String

    fun digest(value: String): ByteArray
}
