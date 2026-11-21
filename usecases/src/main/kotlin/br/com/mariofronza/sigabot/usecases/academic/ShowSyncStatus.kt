package br.com.mariofronza.sigabot.usecases.academic

import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.usecases.academic.port.SyncQueue
import br.com.mariofronza.sigabot.usecases.academic.port.SyncStatus
import java.time.Clock

class ShowSyncStatus(private val queue: SyncQueue, private val clock: Clock) {

    operator fun invoke(studentId: UserId): SyncStatus? = queue.statusOf(studentId, clock.instant())
}
