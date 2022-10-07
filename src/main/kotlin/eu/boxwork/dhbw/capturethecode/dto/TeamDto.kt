package eu.boxwork.dhbw.capturethecode.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TeamDto (

    @Schema(description = "the team ID set by the backend.",required = false)
    @JsonProperty("uuid", required = false) val uuid: UUID?,

    @Schema(description = "the team token, the password, to be set on creation.",required = false)
    @JsonProperty("teamToken", required = false) val teamToken: String?,

    @Schema(description = "the name of the team, must be unique.",required = true)
    @JsonProperty("teamName", required = true)  val teamName: String
)