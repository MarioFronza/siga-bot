package br.com.mariofronza.sigabot.usecases.siga.semester.gateway

import br.com.mariofronza.sigabot.entity.group.Period
import br.com.mariofronza.sigabot.entity.semester.SemesterResult
import br.com.mariofronza.sigabot.entity.subject.SubjectEnrollment
import br.com.mariofronza.sigabot.entity.user.User

interface SemesterResultsInteractor {

    fun findAllByUserAndPeriod(user: User, period: Period): List<SemesterResult>

    fun findBySubjectEnrollmentAndUser(subjectEnrollment: SubjectEnrollment, user: User): SemesterResult?

    fun save(semesterResult: SemesterResult): SemesterResult
}
