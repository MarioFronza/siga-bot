package br.com.mariofronza.sigabot.http.controller.me

import com.fasterxml.jackson.annotation.JsonProperty

data class SyncInput(
    val scope: SyncScope = SyncScope.TERM,
    val year: Int? = null,
    val term: Int? = null,
)

enum class SyncScope {
    @JsonProperty("term")
    TERM,

    @JsonProperty("full")
    FULL,
}
