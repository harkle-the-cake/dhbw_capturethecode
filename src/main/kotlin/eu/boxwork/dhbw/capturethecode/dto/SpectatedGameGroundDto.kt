package eu.boxwork.dhbw.capturethecode.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

data class SpectatedGameGroundDto (
    @Schema(description = "the ID.",required = true)
    @JsonProperty("id", required = true) val id: UUID,

    @Schema(description = "the team name A.",required = true)
    @JsonProperty("teamA", required = true) val teamA: String?,

    @Schema(description = "the team name B.",required = true)
    @JsonProperty("teamB", required = true) val teamB: String?,
)