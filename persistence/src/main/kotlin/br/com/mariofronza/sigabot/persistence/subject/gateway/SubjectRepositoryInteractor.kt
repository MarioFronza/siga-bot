package br.com.mariofronza.sigabot.persistence.subject.gateway

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.persistence.institution.repository.InstitutionRepository
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectJPA
import br.com.mariofronza.sigabot.persistence.subject.repository.SubjectRepository
import br.com.mariofronza.sigabot.usecases.siga.subject.gateway.SubjectInteractor
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class SubjectRepositoryInteractor(
    private val subjectRepository: SubjectRepository,
    private val institutionRepository: InstitutionRepository,
) : SubjectInteractor {

    override fun findByName(institution: Institution, name: String): Subject? =
        subjectRepository.findByInstitutionIdAndName(institution.id!!, name)?.let { subject -> Subject(subject.id, institution, name) }

    override fun save(subject: Subject): Subject {
        val saved = subjectRepository.save(
            SubjectJPA(institution = institutionRepository.getReferenceById(subject.institution.id!!), name = subject.name)
        )
        return subject.copy(id = saved.id)
    }
}
