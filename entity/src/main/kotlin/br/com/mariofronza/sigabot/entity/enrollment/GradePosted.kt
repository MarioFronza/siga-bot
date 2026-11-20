package br.com.mariofronza.sigabot.entity.enrollment

import br.com.mariofronza.sigabot.entity.catalog.SubjectOffering
import br.com.mariofronza.sigabot.entity.user.UserId

data class GradePosted(
    val studentId: UserId,
    val offering: SubjectOffering,
    val grade: Grade,
)
