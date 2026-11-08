package br.com.mariofronza.sigabot.persistence.user.gateway

import br.com.mariofronza.sigabot.entity.user.SigaCredential
import br.com.mariofronza.sigabot.persistence.user.repository.SigaCredentialRepository
import br.com.mariofronza.sigabot.usecases.user.gateway.SigaCredentialInteractor
import org.springframework.stereotype.Component

@Component
class SigaCredentialRepositoryInteractor(
    private val sigaCredentialRepository: SigaCredentialRepository
) : SigaCredentialInteractor {

    override fun findByCpf(cpf: String): SigaCredential? {
        return sigaCredentialRepository.findByCpfHash(cpf)?.toEntity()
    }
}
