package br.com.mariofronza.sigabot.usecases.identity.port

fun interface PasswordHasher {

    fun hash(rawPassword: String): String
}
