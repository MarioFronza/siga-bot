package br.com.mariofronza.sigabot.persistence

import br.com.mariofronza.sigabot.entity.catalog.SubjectOffering
import br.com.mariofronza.sigabot.entity.enrollment.Attendance
import br.com.mariofronza.sigabot.entity.enrollment.Grade
import br.com.mariofronza.sigabot.entity.enrollment.SemesterOutcome
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode
import br.com.mariofronza.sigabot.entity.term.AcademicPeriod
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.enrollment.JooqEnrollmentRepository
import br.com.mariofronza.sigabot.persistence.notification.JooqOutbox
import br.com.mariofronza.sigabot.persistence.user.JooqUserRepository
import br.com.mariofronza.sigabot.usecases.academic.SyncStudentGrades
import br.com.mariofronza.sigabot.usecases.academic.port.AcademicRecord
import br.com.mariofronza.sigabot.usecases.academic.port.AcademicSystem
import br.com.mariofronza.sigabot.usecases.academic.port.SyncScope
import br.com.mariofronza.sigabot.usecases.identity.port.SecretCipher
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import org.jooq.DSLContext
import org.jooq.ExecuteContext
import org.jooq.ExecuteListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.jooq.test.autoconfigure.JooqTest
import org.springframework.context.annotation.Import

@JooqTest
@ImportAutoConfiguration(FlywayAutoConfiguration::class)
@Import(JooqUserRepository::class, JooqEnrollmentRepository::class, JooqOutbox::class, SpringTransactionRunner::class)
class SyncCostTest : FunSpec() {

    @Autowired lateinit var users: JooqUserRepository
    @Autowired lateinit var enrollments: JooqEnrollmentRepository
    @Autowired lateinit var outbox: JooqOutbox
    @Autowired lateinit var transaction: SpringTransactionRunner
    @Autowired lateinit var dsl: DSLContext

    private val period = AcademicPeriod(2026, 1)
    private val siga = object : AcademicSystem {
        override val institution = InstitutionCode.UDESC
        var grade = 8.0
        override fun fetch(credentials: AcademicSystem.Credentials, scope: SyncScope) = AcademicRecord(
            studentName = "Ana Souza",
            periods = listOf(period),
            results = (1..6).map {
                AcademicRecord.Result(
                    SubjectOffering(InstitutionCode.UDESC, period, "ESO", "ESO-1", "Disciplina $it", 72),
                    SemesterOutcome(Grade(if (it == 1) grade else 8.0), 0, Attendance(100.0), "Aprovado"),
                )
            },
        )
    }
    private val cipher = object : SecretCipher {
        override fun encrypt(plainText: String) = plainText
        override fun decrypt(cipherText: String) = cipherText
        override fun digest(value: String) = value.toByteArray()
    }

    private fun rowWrites(): Long =
        dsl.fetchValue("select coalesce(sum(n_tup_upd + n_tup_ins + n_tup_del), 0) from pg_stat_xact_user_tables").toString().toLong()

    init {
        extension(SpringExtension())

        test("an unchanged sync writes nothing and costs a handful of statements") {
            val sync = SyncStudentGrades(users, enrollments, listOf(siga), cipher, outbox, transaction)
            val student = users.save(User.registerStudent(Email.of("ana@test.com"), "h", AcademicAccount(InstitutionCode.UDESC, "l", byteArrayOf(1), "p")))
            sync(student, SyncScope.Period(period))
            val stored = users.findById(student.id) ?: error("student was saved")

            var statements = 0
            dsl.configuration().set(object : ExecuteListener {
                override fun executeStart(ctx: ExecuteContext) { statements++ }
            })
            val writesBefore = rowWrites()
            sync(stored, SyncScope.Period(period))
            val writes = rowWrites() - writesBefore
            dsl.configuration().set(*emptyArray<ExecuteListener>())

            writes shouldBe 0
            statements shouldBeLessThanOrEqual 3
        }

        test("a changed grade writes only that enrollment and its push") {
            val sync = SyncStudentGrades(users, enrollments, listOf(siga), cipher, outbox, transaction)
            val student = users.save(User.registerStudent(Email.of("bia@test.com"), "h", AcademicAccount(InstitutionCode.UDESC, "l2", byteArrayOf(2), "p")))
            sync(student, SyncScope.Period(period))
            val stored = users.findById(student.id) ?: error("student was saved")
            siga.grade = 9.5

            val writesBefore = rowWrites()
            sync(stored, SyncScope.Period(period))

            (rowWrites() - writesBefore) shouldBe 2
            outbox.countPending(maxAttempts = 10) shouldBe 1
        }
    }
}
