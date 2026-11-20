package br.com.mariofronza.sigabot.usecases.credential.gateway

interface SecretCipher {

    fun encrypt(plainText: String): String

    fun decrypt(cipherText: String): String

    fun digest(value: String): ByteArray
}
