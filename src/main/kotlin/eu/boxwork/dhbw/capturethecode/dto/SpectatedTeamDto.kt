package eu.boxwork.dhbw.capturethecode.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.Pattern

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SpectatedTeamDto (

    @Schema(description = "the team ID set by the backend.",required = false)
    @JsonProperty("id", required = false) val id: UUID?,

    @Schema(description = "the name of the team, must be unique.",required = true)
    @JsonProperty("name", required = true)
    val name: String
)