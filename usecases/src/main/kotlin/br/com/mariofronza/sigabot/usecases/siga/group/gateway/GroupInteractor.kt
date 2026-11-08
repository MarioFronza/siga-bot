package br.com.mariofronza.sigabot.usecases.siga.group.gateway

import br.com.mariofronza.sigabot.entity.group.Group

interface GroupInteractor {

    fun findByName(name: String): Group?

    fun save(group: Group): Group
}
