package br.com.mariofronza.sigabot.persistence.subject.gateway

import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.persistence.misc.toJPA
import br.com.mariofronza.sigabot.persistence.subject.repository.SubjectRepository
import br.com.mariofronza.sigabot.usecases.siga.subject.gateway.SubjectInteractor
import org.springframework.stereotype.Component

@Component
class SubjectRepositoryInteractor(
    private val subjectRepository: SubjectRepository
) : SubjectInteractor {

    override fun findByName(name: String): Subject? {
        return subjectRepository.findByName(name)?.toEntity()
    }

    override fun save(subject: Subject): Subject {
        return subjectRepository.save(subject.toJPA()).toEntity()
    }
}
