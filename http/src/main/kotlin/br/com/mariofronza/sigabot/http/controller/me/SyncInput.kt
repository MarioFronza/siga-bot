package br.com.mariofronza.sigabot.http.controller.me

import com.fasterxml.jackson.annotation.JsonProperty

data class SyncInput(
    val scope: Scope = Scope.TERM,
    val year: Int? = null,
    val term: Int? = null,
) {
    enum class Scope {
        @JsonProperty("term")
        TERM,

        @JsonProperty("full")
        FULL,
    }
}
