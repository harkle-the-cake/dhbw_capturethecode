package eu.boxwork.dhbw.capturethecode.dto

import com.fasterxml.jackson.annotation.JsonProperty
import eu.boxwork.dhbw.capturethecode.model.Event
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*
import java.util.concurrent.LinkedTransferQueue

data class SpectatedGameGroundDto (
    @Schema(description = "the ID.",required = true)
    @JsonProperty("id", required = true) val id: UUID,

    @Schema(description = "the team name A.",required = true)
    @JsonProperty("teamA", required = true) val teamA: String?,

    @Schema(description = "the team name B.",required = false)
    @JsonProperty("teamB", required = false) val teamB: String?,

    @Schema(description = "events that happened so far.",required = false)
    @JsonProperty("events", required = false) val events: LinkedTransferQueue<Event>?,

    @Schema(description = "TRAINING ONLY: player infos.",required = false)
    @JsonProperty("players", required = false) val players: LinkedTransferQueue<SpectatedPlayerInfo>?
)