package eu.boxwork.dhbw.capturethecode.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ScoreDto (
    @Schema(description = "the first team name.",required = true)
    @JsonProperty("teamA", required = true) val teamA: String,

    @Schema(description = "score of the first team.",required = true)
    @JsonProperty("scoreTeamA", required = true) val scoreTeamA: Int,

    @Schema(description = "the second team name.",required = false)
    @JsonProperty("teamB", required = false) val teamB: String?,

    @Schema(description = "score of the second team.",required = false)
    @JsonProperty("scoreTeamB", required = false) val scoreTeamB: Int?,
    
    @Schema(description = "the current round.",required = false)
    @JsonProperty("round", required = false) val round: Int?,
)