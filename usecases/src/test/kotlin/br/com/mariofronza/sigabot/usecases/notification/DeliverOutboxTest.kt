package br.com.mariofronza.sigabot.usecases.notification

import br.com.mariofronza.sigabot.entity.user.UserId
import br.com.mariofronza.sigabot.usecases.notification.port.Outbox
import br.com.mariofronza.sigabot.usecases.notification.port.OutboxMessage
import br.com.mariofronza.sigabot.usecases.notification.port.PushSender
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class DeliverOutboxTest : FunSpec({

    test("delivers pending pushes and keeps the ones that fail for a retry") {
        val outbox: Outbox = mockk(relaxed = true)
        val pushSender: PushSender = mockk()
        val ana = UserId.generate()
        val bia = UserId.generate()
        every { outbox.pending(50, DeliverOutbox.MAX_ATTEMPTS) } returns listOf(OutboxMessage(1, ana, "oi Ana"), OutboxMessage(2, bia, "oi Bia"))
        every { pushSender.sendTo(ana, "oi Ana") } returns Unit
        every { pushSender.sendTo(bia, "oi Bia") } throws IllegalStateException("OneSignal down")

        DeliverOutbox(outbox, pushSender)() shouldBe 1

        verify { outbox.delivered(1) }
        verify { outbox.failed(2) }
        verify(exactly = 0) { outbox.delivered(2) }
    }
})
