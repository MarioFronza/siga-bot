package br.com.mariofronza.sigabot.usecases.dashboard.dto

import br.com.mariofronza.sigabot.usecases.siga.term.dto.AcademicTermOutput

data class DashboardOutput(
    val period: AcademicTermOutput,
    val results: List<ResultOutput>
)

data class ResultOutput(
    val finalGrade: Double,
    val courseLoad: Int,
    val absencesCount: Int,
    val attendancePercentage: Double,
    val result: String,
    val subjectName: String,
    val groupName: String
)
