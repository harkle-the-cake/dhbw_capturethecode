package eu.boxwork.dhbw.capturethecode.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import eu.boxwork.dhbw.capturethecode.model.Player
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TeamWithMembersDto (
    @Schema(description = "the team ID set by the backend.",required = false)
    @JsonProperty("uuid", required = false) val uuid: UUID?,

    @Schema(description = "the name of the team, must be unique.",required = true)
    @JsonProperty("teamName", required = true)  val teamName: String,

    @Schema(description = "the list of team members.",required = true)
    @JsonProperty("teamMembers", required = true)  val teamMembers: MutableList<PlayerDto>
)