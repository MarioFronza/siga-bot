package br.com.mariofronza.sigabot.usecases.siga.subject

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.usecases.siga.subject.gateway.SubjectInteractor
import org.springframework.stereotype.Service

@Service
class SubjectService(
    private val subjectInteractor: SubjectInteractor
) {

    fun findOrCreate(institution: Institution, name: String): Subject {
        return subjectInteractor.findByName(institution, name)
            ?: subjectInteractor.save(Subject(institution = institution, name = name))
    }
}
