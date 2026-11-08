package br.com.mariofronza.sigabot.usecases.dashboard.dto

import br.com.mariofronza.sigabot.entity.group.Period

data class DashboardOutput(
    val period: Period,
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
