package br.com.mariofronza.sigabot.usecases.academic

import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.usecases.academic.port.SyncQueue
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import java.time.Clock

class RequestSync(private val queue: SyncQueue, private val clock: Clock) {

    operator fun invoke(studentId: UserId, scope: SyncScope) = queue.request(studentId, scope, clock.instant())
}
