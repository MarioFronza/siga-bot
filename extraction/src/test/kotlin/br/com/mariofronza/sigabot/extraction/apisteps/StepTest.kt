package br.com.mariofronza.sigabot.extraction.apisteps

import br.com.mariofronza.sigabot.usecases.academic.port.SigaLayoutChangedException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import okhttp3.ResponseBody
import retrofit2.Response

class StepTest : FunSpec({

    val step = object : Step() {}

    test("reports a layout change when a successful response has no body") {
        shouldThrow<SigaLayoutChangedException> { with(step) { Response.success<ResponseBody>(null).getBody() } }
    }
})
