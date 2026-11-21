package br.com.mariofronza.sigabot.usecases.notification

import br.com.mariofronza.sigabot.entity.notification.NotificationKind
import br.com.mariofronza.sigabot.usecases.notification.port.NotificationRepository
import br.com.mariofronza.sigabot.usecases.notification.port.PushSender
import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
import io.mockk.verifyOrder

class BroadcastNotificationTest : FunSpec({

    test("keeps the announcement in every active student's notifications and pushes it") {
        val notifications: NotificationRepository = mockk(relaxed = true)
        val pushSender: PushSender = mockk(relaxed = true)

        BroadcastNotification(notifications, pushSender)("As notas saíram")

        verifyOrder {
            notifications.saveForEveryActiveUser("As notas saíram", NotificationKind.ANNOUNCEMENT)
            pushSender.sendToAll("As notas saíram")
        }
    }
})
