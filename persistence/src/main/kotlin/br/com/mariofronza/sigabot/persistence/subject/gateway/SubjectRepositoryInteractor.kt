package br.com.mariofronza.sigabot.persistence.subject.gateway

import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectJPA
import br.com.mariofronza.sigabot.persistence.subject.repository.SubjectRepository
import br.com.mariofronza.sigabot.usecases.siga.subject.gateway.SubjectInteractor
import org.springframework.stereotype.Component

@Component
class SubjectRepositoryInteractor(
    private val subjectRepository: SubjectRepository,
) : SubjectInteractor {

    override fun findByName(name: String): Subject? = subjectRepository.findByName(name)?.toEntity()

    override fun save(subject: Subject): Subject = subjectRepository.save(SubjectJPA(name = subject.name)).toEntity()
}
