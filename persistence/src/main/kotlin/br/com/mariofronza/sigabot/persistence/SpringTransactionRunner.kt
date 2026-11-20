package br.com.mariofronza.sigabot.persistence

import br.com.mariofronza.sigabot.usecases.shared.TransactionRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@Component
class SpringTransactionRunner(transactionManager: PlatformTransactionManager) : TransactionRunner {

    private val template = TransactionTemplate(transactionManager)

    override fun <T> inTransaction(block: () -> T): T {
        val result = mutableListOf<T>()
        template.executeWithoutResult { result += block() }
        return result.single()
    }
}
