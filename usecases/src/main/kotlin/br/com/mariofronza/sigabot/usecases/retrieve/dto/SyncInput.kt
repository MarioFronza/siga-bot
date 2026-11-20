package br.com.mariofronza.sigabot.usecases.retrieve.dto

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
