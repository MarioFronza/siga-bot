package br.com.mariofronza.sigabot.usecases.siga.subject

import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.usecases.siga.subject.gateway.SubjectOfferingInteractor
import org.springframework.stereotype.Service

@Service
class SubjectOfferingService(
    private val subjectOfferingInteractor: SubjectOfferingInteractor
) {

    fun findOrCreate(subject: Subject, classSection: ClassSection, courseLoad: Int): SubjectOffering {
        return subjectOfferingInteractor.find(subject, classSection)
            ?: subjectOfferingInteractor.save(
                SubjectOffering(subject = subject, classSection = classSection, courseLoad = courseLoad)
            )
    }
}
