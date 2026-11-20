package br.com.mariofronza.sigabot.usecases.shared

interface TransactionRunner {

    fun <T> inTransaction(block: () -> T): T
}
