package br.com.mariofronza.sigabot.usecases.siga.subject

import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.usecases.siga.subject.gateway.SubjectInteractor
import org.springframework.stereotype.Service

@Service
class SubjectService(
    private val subjectInteractor: SubjectInteractor
) {

    fun createSubject(subjectName: String): Subject {
        var subject = subjectInteractor.findByName(subjectName)

        if (subject == null) {
            subject = subjectInteractor.save(
                Subject(
                    name = subjectName
                )
            )
        }

        return subject
    }
}
