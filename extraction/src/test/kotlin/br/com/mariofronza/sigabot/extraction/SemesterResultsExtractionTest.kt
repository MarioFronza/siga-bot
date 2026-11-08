package br.com.mariofronza.sigabot.extraction

import br.com.mariofronza.sigabot.extraction.apisteps.StepResponse.StepError
import br.com.mariofronza.sigabot.extraction.extractor.ExtractorContext
import br.com.mariofronza.sigabot.extraction.extractor.prelogin.CookieExtractor
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.PeriodInput
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SemesterResultsExtractionInput
import br.com.mariofronza.sigabot.usecases.extraction.semester.dto.SigaCredentialInput
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk

class SemesterResultsExtractionTest : FunSpec({

    test("gives every extraction its own SIGA session context") {
        val contexts = mutableListOf<ExtractorContext>()
        val cookieExtractor: CookieExtractor = mockk()
        every { cookieExtractor.execute(any(), any()) } answers {
            contexts.add(secondArg())
            StepError("offline")
        }
        val extraction = SemesterResultsExtraction(
            cookieExtractor,
            mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), mockk()
        )
        val input = SemesterResultsExtractionInput(SigaCredentialInput("cpf", "password"), PeriodInput("2026", 2))

        repeat(2) { runCatching { extraction.extract(input, completeExtraction = false) } }

        contexts[0] shouldNotBeSameInstanceAs contexts[1]
    }
})
