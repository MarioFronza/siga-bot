package br.com.mariofronza.sigabot.persistence.group.gateway

import br.com.mariofronza.sigabot.entity.group.Group
import br.com.mariofronza.sigabot.persistence.group.repository.GroupRepository
import br.com.mariofronza.sigabot.persistence.misc.toJPA
import br.com.mariofronza.sigabot.usecases.siga.group.gateway.GroupInteractor
import org.springframework.stereotype.Component

@Component
class GroupRepositoryInteractor(
    private val groupRepository: GroupRepository
) : GroupInteractor {

    override fun findByName(name: String): Group? {
        return groupRepository.findByName(name)?.toEntity()
    }

    override fun save(group: Group): Group {
        return groupRepository.save(group.toJPA()).toEntity()
    }
}
