package br.com.mariofronza.sigabot.persistence.user.gateway

import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.persistence.user.entity.RoleJPA
import br.com.mariofronza.sigabot.persistence.user.repository.RoleRepository
import br.com.mariofronza.sigabot.usecases.user.gateway.RoleInteractor
import org.springframework.stereotype.Component

@Component
class RoleRepositoryInteractor(
    private val roleRepository: RoleRepository
) : RoleInteractor {

    override fun findByName(name: String): Role? {
        return roleRepository.findByName(name)?.toEntity()
    }

    override fun save(role: Role): Role {
        val roleJPA = RoleJPA(
            name = role.name
        )
        return roleRepository.save(roleJPA).toEntity()
    }
}
