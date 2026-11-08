package br.com.mariofronza.sigabot.extraction.apisteps

sealed class StepResponse {

    data class StepSuccess<T>(val payload: T) : StepResponse()

    data class StepError(val errorMessage: String) : StepResponse()
}
