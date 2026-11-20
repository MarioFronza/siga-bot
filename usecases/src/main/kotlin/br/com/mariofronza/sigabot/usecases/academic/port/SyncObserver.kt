package br.com.mariofronza.sigabot.usecases.academic.port

import java.time.Duration

interface SyncObserver {

    fun synced(duration: Duration)

    fun failed(error: Throwable)
}
