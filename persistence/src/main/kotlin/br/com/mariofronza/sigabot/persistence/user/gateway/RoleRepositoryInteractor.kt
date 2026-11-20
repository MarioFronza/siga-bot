package br.com.mariofronza.sigabot.persistence.user.gateway

import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.persistence.user.repository.RoleRepository
import br.com.mariofronza.sigabot.usecases.user.gateway.RoleInteractor
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class RoleRepositoryInteractor(
    private val roleRepository: RoleRepository,
) : RoleInteractor {

    override fun findByName(name: String): Role? = roleRepository.findByName(name)?.toEntity()
}
