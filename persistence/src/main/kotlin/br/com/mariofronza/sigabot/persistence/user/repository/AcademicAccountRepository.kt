package br.com.mariofronza.sigabot.persistence.user.repository

import br.com.mariofronza.sigabot.persistence.user.entity.AcademicAccountJPA
import org.springframework.data.jpa.repository.JpaRepository

interface AcademicAccountRepository : JpaRepository<AcademicAccountJPA, Long> {

    fun existsByInstitutionIdAndLoginDigest(institutionId: Long, loginDigest: ByteArray): Boolean
}
