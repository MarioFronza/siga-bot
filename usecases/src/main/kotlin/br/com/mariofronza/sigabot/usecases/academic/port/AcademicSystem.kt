package br.com.mariofronza.sigabot.usecases.academic.port

import br.com.mariofronza.sigabot.entity.catalog.SubjectOffering
import br.com.mariofronza.sigabot.entity.enrollment.SemesterOutcome
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod

interface AcademicSystem {

    val institution: InstitutionCode

    fun fetch(credentials: Credentials, scope: SyncScope): AcademicRecord

    data class Credentials(val login: String, val password: String)
}

sealed interface SyncScope {
    data class Period(val period: AcademicPeriod) : SyncScope
    data object Everything : SyncScope
}

data class AcademicRecord(
    val studentName: String,
    val periods: List<AcademicPeriod>,
    val results: List<Result>,
) {
    data class Result(val offering: SubjectOffering, val outcome: SemesterOutcome)
}
