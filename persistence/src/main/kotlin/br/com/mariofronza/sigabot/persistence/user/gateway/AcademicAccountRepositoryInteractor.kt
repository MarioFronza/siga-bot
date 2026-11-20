package br.com.mariofronza.sigabot.persistence.user.gateway

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.persistence.user.repository.AcademicAccountRepository
import br.com.mariofronza.sigabot.usecases.user.gateway.AcademicAccountInteractor
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class AcademicAccountRepositoryInteractor(
    private val academicAccountRepository: AcademicAccountRepository,
) : AcademicAccountInteractor {

    override fun existsByLoginDigest(institution: Institution, loginDigest: ByteArray): Boolean =
        academicAccountRepository.existsByInstitutionIdAndLoginDigest(institution.id!!, loginDigest)
}
