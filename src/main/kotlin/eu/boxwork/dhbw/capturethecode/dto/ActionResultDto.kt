package eu.boxwork.dhbw.capturethecode.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ActionResultDto (
    @Schema(description = "the current round.",required = true)
    @JsonProperty("round", required = true) val round: Int,

    @Schema(description = "the maximum rounds.",required = true)
    @JsonProperty("maxRound", required = true) val maxRound: Int,

    @Schema(description = "the player state of the acting player.",required = true)
    @JsonProperty("state", required = true) val state: String,

    @Schema(description = "true, if the target has the flag.",required = true)
    @JsonProperty("hasFlag", required = true) val hasFlag: Boolean,

    @Schema(description = "true, if the acting player has the flag.",required = true)
    @JsonProperty("haveFlag", required = true) val haveFlag: Boolean,

    @Schema(description = "true, if the game is over.",required = true)
    @JsonProperty("gameOver", required = true) val gameOver: Boolean = false,

    @Schema(description = "the state of the target player, if looked at.",required = false)
    @JsonProperty("targetState", required = false) val targetState: PlayerStateDto?,

    @Schema(description = "the state of all players, if looked at.",required = false)
    @JsonProperty("targetStates", required = false) val targetStates: MutableList<PlayerStateDto>?
)