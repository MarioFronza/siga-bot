package br.com.mariofronza.sigabot.usecases.user.gateway

import br.com.mariofronza.sigabot.entity.user.Role

interface RoleInteractor {

    fun findByName(name: String): Role?

    fun save(role: Role): Role
}
