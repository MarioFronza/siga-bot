package br.com.mariofronza.sigabot.persistence.user.repository

import br.com.mariofronza.sigabot.persistence.user.entity.SigaCredentialJPA
import org.springframework.data.jpa.repository.JpaRepository

interface SigaCredentialRepository : JpaRepository<SigaCredentialJPA, Long> {

    fun existsByCpfDigest(cpfDigest: ByteArray): Boolean
}
