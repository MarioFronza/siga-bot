package br.com.mariofronza.sigabot.usecases.credential.gateway

fun interface PasswordHasher {

    fun hash(rawPassword: String): String
}
