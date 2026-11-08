package br.com.mariofronza.sigabot.persistence.semester.gateway

import br.com.mariofronza.sigabot.entity.group.Period
import br.com.mariofronza.sigabot.entity.semester.SemesterResult
import br.com.mariofronza.sigabot.entity.subject.SubjectEnrollment
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.misc.toJPA
import br.com.mariofronza.sigabot.persistence.semester.repository.SemesterRepository
import br.com.mariofronza.sigabot.usecases.siga.semester.gateway.SemesterResultsInteractor
import org.springframework.stereotype.Component

@Component
class SemesterResultRepositoryInteractor(
    private val semesterRepository: SemesterRepository
) : SemesterResultsInteractor {

    override fun findAllByUserAndPeriod(user: User, period: Period): List<SemesterResult> {
        return semesterRepository.findAllByUserAndPeriod(user.toJPA(), period.year, period.term).map { it.toEntity() }
    }

    override fun findBySubjectEnrollmentAndUser(subjectEnrollment: SubjectEnrollment, user: User): SemesterResult? {
        return semesterRepository.findBySubjectEnrolledAndUser(subjectEnrollment.toJPA(), user.toJPA())?.toEntity()
    }

    override fun save(semesterResult: SemesterResult): SemesterResult {
        return semesterRepository.save(semesterResult.toJPA()).toEntity()
    }
}
