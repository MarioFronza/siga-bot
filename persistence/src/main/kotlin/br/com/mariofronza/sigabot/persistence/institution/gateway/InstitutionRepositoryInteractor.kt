package br.com.mariofronza.sigabot.persistence.institution.gateway

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.persistence.institution.repository.InstitutionRepository
import br.com.mariofronza.sigabot.usecases.institution.gateway.InstitutionInteractor
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class InstitutionRepositoryInteractor(
    private val institutionRepository: InstitutionRepository,
) : InstitutionInteractor {

    override fun findByCode(code: String): Institution? = institutionRepository.findByCode(code)?.toEntity()
}
