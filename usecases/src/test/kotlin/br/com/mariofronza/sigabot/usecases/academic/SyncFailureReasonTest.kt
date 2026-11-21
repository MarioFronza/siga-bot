package br.com.mariofronza.sigabot.usecases.academic

import br.com.mariofronza.sigabot.usecases.academic.port.SigaCredentialRejectedException
import br.com.mariofronza.sigabot.usecases.academic.port.SigaLayoutChangedException
import br.com.mariofronza.sigabot.usecases.academic.port.SigaPeriodNotFoundException
import br.com.mariofronza.sigabot.usecases.academic.port.SigaUnavailableException
import br.com.mariofronza.sigabot.usecases.academic.port.SyncFailureReason
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SyncFailureReasonTest : FunSpec({

    test("tells a rejected credential, an outage and a layout change apart") {
        SyncFailureReason.of(SigaCredentialRejectedException()) shouldBe SyncFailureReason.INVALID_CREDENTIAL
        SyncFailureReason.of(SigaUnavailableException("SIGA did not answer")) shouldBe SyncFailureReason.SIGA_UNAVAILABLE
        SyncFailureReason.of(SigaLayoutChangedException("SIGA results have no table")) shouldBe SyncFailureReason.SIGA_CHANGED
    }

    test("calls anything else unknown") {
        SyncFailureReason.of(SigaPeriodNotFoundException("2030/1")) shouldBe SyncFailureReason.UNKNOWN
        SyncFailureReason.of(IllegalStateException("database down")) shouldBe SyncFailureReason.UNKNOWN
    }
})
