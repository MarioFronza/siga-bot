package br.com.mariofronza.sigabot.usecases.siga.group.gateway

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.group.Group
import br.com.mariofronza.sigabot.entity.group.Period

interface GroupInteractor {

    fun find(name: String, course: Course, period: Period): Group?

    fun save(group: Group): Group
}
