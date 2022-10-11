package eu.boxwork.dhbw.capturethecode.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.Pattern

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TeamDto (

    @Schema(description = "the team ID set by the backend.",required = false)
    @JsonProperty("uuid", required = false) val uuid: UUID?,

    @Schema(description = "the team token, the password, to be set on creation.",required = false)
    @JsonProperty("teamToken", required = false)
    @field:Pattern(regexp = "[0-9A-Fa-f]{64}", message = "must be 32 byte HEX")
    val teamToken: String?,

    @Schema(description = "the name of the team, must be unique.",required = true)
    @JsonProperty("teamName", required = true)
    @field:Pattern(regexp = "\\w{1,20}", message = "up to 20 characters are allowed")
    val teamName: String
)