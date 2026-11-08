package br.com.mariofronza.sigabot.usecases.user.gateway

import br.com.mariofronza.sigabot.entity.user.SigaCredential

interface SigaCredentialInteractor {

    fun findByCpf(cpf: String): SigaCredential?
}
