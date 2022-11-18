package eu.boxwork.dhbw.capturethecode.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Event(
    @JsonProperty("round", required = false) val round : Int,
    @JsonProperty("source", required = false) val source: String?,
    @JsonProperty("target", required = false) val target: String?,
    @JsonProperty("event", required = false) val event: String,
)
