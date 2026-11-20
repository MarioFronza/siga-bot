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
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class DeliverOutboxTest : FunSpec({

    val now = Instant.parse("2026-11-20T12:00:00Z")

    test("delivers claimed pushes and backs off the ones that fail") {
        val outbox: Outbox = mockk(relaxed = true)
        val pushSender: PushSender = mockk()
        val ana = UserId.generate()
        val bia = UserId.generate()
        every { outbox.claimPending(now, 50, Duration.ofMinutes(5), DeliverOutbox.MAX_ATTEMPTS) } returns
            listOf(OutboxMessage(1, ana, "oi Ana", attempts = 0), OutboxMessage(2, bia, "oi Bia", attempts = 2))
        every { pushSender.sendTo(ana, "oi Ana") } returns Unit
        every { pushSender.sendTo(bia, "oi Bia") } throws IllegalStateException("OneSignal down")

        DeliverOutbox(outbox, pushSender, Clock.fixed(now, ZoneOffset.UTC))() shouldBe 1

        verify { outbox.delivered(1) }
        verify { outbox.failed(2, now.plus(Duration.ofMinutes(2))) }
        verify(exactly = 0) { outbox.delivered(2) }
    }

    test("doubles the retry delay up to a ceiling") {
        val deliver = DeliverOutbox(mockk(), mockk(), Clock.fixed(now, ZoneOffset.UTC))

        deliver.retryDelay(1) shouldBe Duration.ofSeconds(30)
        deliver.retryDelay(3) shouldBe Duration.ofMinutes(2)
        deliver.retryDelay(10) shouldBe Duration.ofHours(1)
    }
})
