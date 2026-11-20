package br.com.mariofronza.sigabot.persistence.user.gateway

import br.com.mariofronza.sigabot.persistence.user.repository.SigaCredentialRepository
import br.com.mariofronza.sigabot.usecases.user.gateway.SigaCredentialInteractor
import org.springframework.stereotype.Component

@Component
class SigaCredentialRepositoryInteractor(
    private val sigaCredentialRepository: SigaCredentialRepository,
) : SigaCredentialInteractor {

    override fun existsByCpfDigest(cpfDigest: ByteArray): Boolean =
        sigaCredentialRepository.existsByCpfDigest(cpfDigest)
}
