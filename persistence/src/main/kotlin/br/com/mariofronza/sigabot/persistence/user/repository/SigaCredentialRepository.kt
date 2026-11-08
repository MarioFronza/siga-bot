package br.com.mariofronza.sigabot.persistence.user.repository

import br.com.mariofronza.sigabot.persistence.user.entity.SigaCredentialJPA
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SigaCredentialRepository : JpaRepository<SigaCredentialJPA, Long> {

    fun findByCpfHash(cpf: String): SigaCredentialJPA?
}
