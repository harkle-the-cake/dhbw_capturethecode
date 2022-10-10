package eu.boxwork.dhbw.capturethecode.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PlayerStateDto (
    @Schema(description = "the player ID set by the backend.",required = false)
    @JsonProperty("uuid", required = false) val uuid: UUID?,

    @Schema(description = "the player name; must be unique.",required = false)
    @JsonProperty("name", required = false) val name: String?,

    @Schema(description = "the player state.",required = true)
    @JsonProperty("state", required = true) val state: String,

    @Schema(description = "true, if the target has the flag.",required = true)
    @JsonProperty("hasFlag", required = true) val hasFlag: Boolean,
)