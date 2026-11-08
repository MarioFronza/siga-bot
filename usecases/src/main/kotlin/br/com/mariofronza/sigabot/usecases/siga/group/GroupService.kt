package br.com.mariofronza.sigabot.usecases.siga.group

import br.com.mariofronza.sigabot.entity.course.Course
import br.com.mariofronza.sigabot.entity.group.Group
import br.com.mariofronza.sigabot.entity.group.Period
import br.com.mariofronza.sigabot.usecases.siga.group.gateway.GroupInteractor
import org.springframework.stereotype.Service

@Service
class GroupService(
    private val groupInteractor: GroupInteractor
) {

    fun createGroup(groupName: String, course: Course, period: Period): Group {
        var group = groupInteractor.findByName(groupName)

        if (group == null) {
            group = groupInteractor.save(
                Group(
                    name = groupName,
                    course = course,
                    period = period
                )
            )
        }

        return group
    }
}
