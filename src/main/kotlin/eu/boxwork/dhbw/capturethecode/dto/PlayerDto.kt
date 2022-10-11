package eu.boxwork.dhbw.capturethecode.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*
import javax.validation.constraints.Pattern

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class PlayerDto (
    @Schema(description = "the player ID set by the backend.",required = false)
    @JsonProperty("uuid", required = false) val uuid: UUID?,

    @Schema(description = "the player name; must be unique.",required = true)
    @JsonProperty("name", required = true)
    @field:Pattern(regexp = "\\w{1,20}", message = "up to 20 characters are allowed") val name: String,

    @Schema(description = "the name of the team, must be unique.",required = true)
    @JsonProperty("teamName", required = true)
    @field:Pattern(regexp = "\\w{1,20}", message = "up to 20 characters are allowed") val teamName: String
)