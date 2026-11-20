package br.com.mariofronza.sigabot.usecases.user.gateway

interface SigaCredentialInteractor {

    fun existsByCpfDigest(cpfDigest: ByteArray): Boolean
}
