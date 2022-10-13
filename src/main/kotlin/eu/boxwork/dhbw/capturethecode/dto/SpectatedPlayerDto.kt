package eu.boxwork.dhbw.capturethecode.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*
import javax.validation.constraints.Pattern

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class SpectatedPlayerDto (
    @Schema(description = "the player name; must be unique.",required = true)
    @JsonProperty("name", required = true)
    val name: String,
)