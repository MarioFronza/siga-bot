package br.com.mariofronza.sigabot.persistence.subject.gateway

import br.com.mariofronza.sigabot.entity.section.ClassSection
import br.com.mariofronza.sigabot.entity.subject.Subject
import br.com.mariofronza.sigabot.entity.subject.SubjectOffering
import br.com.mariofronza.sigabot.persistence.section.repository.ClassSectionRepository
import br.com.mariofronza.sigabot.persistence.subject.entity.SubjectOfferingJPA
import br.com.mariofronza.sigabot.persistence.subject.repository.SubjectOfferingRepository
import br.com.mariofronza.sigabot.persistence.subject.repository.SubjectRepository
import br.com.mariofronza.sigabot.usecases.siga.subject.gateway.SubjectOfferingInteractor
import org.springframework.stereotype.Component

@Component
class SubjectOfferingRepositoryInteractor(
    private val subjectOfferingRepository: SubjectOfferingRepository,
    private val subjectRepository: SubjectRepository,
    private val classSectionRepository: ClassSectionRepository,
) : SubjectOfferingInteractor {

    override fun find(subject: Subject, classSection: ClassSection): SubjectOffering? =
        subjectOfferingRepository.findBySubjectIdAndClassSectionId(subject.id!!, classSection.id!!)
            ?.let { SubjectOffering(id = it.id, subject = subject, classSection = classSection, courseLoad = it.courseLoad) }

    override fun save(subjectOffering: SubjectOffering): SubjectOffering {
        val saved = subjectOfferingRepository.save(
            SubjectOfferingJPA(
                classSection = classSectionRepository.getReferenceById(subjectOffering.classSection.id!!),
                subject = subjectRepository.getReferenceById(subjectOffering.subject.id!!),
                courseLoad = subjectOffering.courseLoad,
            )
        )
        return subjectOffering.copy(id = saved.id)
    }
}
