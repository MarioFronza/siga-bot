package br.com.mariofronza.sigabot.entity.catalog

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod

data class SubjectOffering(
    val institution: InstitutionCode,
    val period: AcademicPeriod,
    val course: String,
    val classSection: String,
    val subject: String,
    val courseLoad: Int,
) {

    init {
        require(course.isNotBlank() && classSection.isNotBlank() && subject.isNotBlank()) {
            "An offering needs a course, a class section and a subject"
        }
        require(courseLoad >= 0) { "Course load $courseLoad cannot be negative" }
    }
}
