package eu.boxwork.dhbw.capturethecode.dto

import com.fasterxml.jackson.annotation.JsonProperty
import eu.boxwork.dhbw.capturethecode.model.Event
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*
import java.util.concurrent.LinkedTransferQueue

data class SpectatedPlayerInfo (
    @Schema(description = "the name.",required = false)
    @JsonProperty("name", required = false) val name: String?,

    @Schema(description = "the current state.",required = false)
    @JsonProperty("state", required = false) val state: String?,

    @Schema(description = "the current action.",required = false)
    @JsonProperty("action", required = false) val action: String?,

    @Schema(description = "token?",required = false)
    @JsonProperty("hasCode", required = false) val hasCode: Boolean?,
)