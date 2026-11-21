package br.com.mariofronza.sigabot.integration.data.remove.onesignal

class OneSignalRejectedException(status: Int, body: String) : RuntimeException("OneSignal answered $status: $body")
