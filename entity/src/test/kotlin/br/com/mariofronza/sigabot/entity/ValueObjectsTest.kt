package br.com.mariofronza.sigabot.entity

import br.com.mariofronza.sigabot.entity.enrollment.Attendance
import br.com.mariofronza.sigabot.entity.enrollment.Grade
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.user.Cpf
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.UserId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ValueObjectsTest : FunSpec({

    test("generates time-ordered user ids") {
        val first = UserId.generate()
        val second = UserId.generate()

        first.value.version() shouldBe 7
        first.value.variant() shouldBe 2
        first shouldNotBe second
        first.value.mostSignificantBits ushr 16 shouldBeLessThanOrEqual(second.value.mostSignificantBits ushr 16)
    }

    test("normalizes an email to lowercase without surrounding spaces") {
        Email.of("  Ana@Test.COM ").value shouldBe "ana@test.com"
    }

    test("rejects an email without an at sign") {
        shouldThrow<IllegalArgumentException> { Email.of("ana.test.com") }
    }

    test("keeps only the digits of a cpf") {
        Cpf.of("123.456.789-00").value shouldBe "12345678900"
    }

    test("rejects a cpf without digits") {
        shouldThrow<IllegalArgumentException> { Cpf.of("abc") }
    }

    test("parses the period SIGA shows") {
        AcademicPeriod.parse("2026/2") shouldBe AcademicPeriod(2026, 2)
    }

    test("orders periods by year then term") {
        AcademicPeriod(2025, 2) shouldBeLessThan AcademicPeriod(2026, 1)
        AcademicPeriod(2026, 1) shouldBeLessThan AcademicPeriod(2026, 2)
    }

    test("rejects a period SIGA would never show") {
        shouldThrow<IllegalArgumentException> { AcademicPeriod.parse("2026") }
        shouldThrow<IllegalArgumentException> { AcademicPeriod(2026, 0) }
    }

    test("rejects a negative grade") {
        shouldThrow<IllegalArgumentException> { Grade(-0.5) }
    }

    test("keeps attendance between zero and one hundred percent") {
        Attendance(100.0).percentage shouldBe 100.0
        shouldThrow<IllegalArgumentException> { Attendance(100.5) }
    }
})

private infix fun Long.shouldBeLessThanOrEqual(other: Long) {
    (this <= other) shouldBe true
}
