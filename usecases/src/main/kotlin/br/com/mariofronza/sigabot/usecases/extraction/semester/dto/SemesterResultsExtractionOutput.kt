package br.com.mariofronza.sigabot.usecases.extraction.semester.dto

data class SemesterResultsExtractionOutput(
    val studentName: String,
    val course: String,
    val semesterResultsPeriod: List<SemesterResultPeriod>
)

data class SemesterResultPeriod(
    val semesterResults: List<SemesterResultOutput>,
    val period: String,
)

data class SemesterResultOutput(
    val subjectName: String,
    val groupName: String,
    val finalGrade: Double,
    val courseLoad: Int,
    val absencesCount: Int,
    val attendancePercentage: Double,
    val result: String,
)
