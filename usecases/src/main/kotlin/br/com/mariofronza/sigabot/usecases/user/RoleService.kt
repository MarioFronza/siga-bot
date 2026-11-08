package br.com.mariofronza.sigabot.usecases.user

import br.com.mariofronza.sigabot.entity.user.Role
import br.com.mariofronza.sigabot.usecases.user.gateway.RoleInteractor
import org.springframework.stereotype.Service

@Service
class RoleService(
    val roleInteractor: RoleInteractor,
) {

    fun findByName(roleName: String): Role? {
        return roleInteractor.findByName(roleName)
    }

    fun createRole(roleName: String): Role {

        if (roleInteractor.findByName(roleName) != null) {
            throw Exception("Role already exists")
        }

        return roleInteractor.save(Role(name = roleName))
    }
}
