package eu.boxwork.dhbw.capturethecode.service

import eu.boxwork.dhbw.capturethecode.dto.ActionResultDto
import eu.boxwork.dhbw.capturethecode.dto.ScoreDto
import eu.boxwork.dhbw.capturethecode.enums.Action
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/competition")
class CompetitionController(
    @Autowired val competitionService: CompetitionService) {

    @Operation(summary = "competition result",
        description = "returns the current competition scores, will increase each time frame if flag not lost.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "the score", content = [
            (Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(description = "the team score", implementation = ScoreDto::class)
            ))]),
        ApiResponse(responseCode = "400", description = "token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "user not authorized, token is wrong", content = [Content()]),
        ApiResponse(responseCode = "404", description = "team not existing", content = [Content()]),
        ApiResponse(responseCode = "412", description = "team not in training", content = [Content()]),
    ]
    )
    @GetMapping("/{id}")
    fun getScore(
        @PathVariable(value = "id") id: UUID
    ) : ResponseEntity<ScoreDto> {
        return ResponseEntity.ok(competitionService.getScore(id))
    }

    @Operation(summary = "Start a competition",
        description = "Starts a new competition.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "competition is started. Another team needs to join."),
        ApiResponse(responseCode = "400", description = "token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "user not authorized, token is wrong", content = [Content()]),
        ApiResponse(responseCode = "404", description = "team not existing", content = [Content()]),
        ApiResponse(responseCode = "412", description = "team has no team members", content = [Content()]),
    ]
    )
    @PostMapping("")
    fun start(auth: Authentication) : ResponseEntity<String> {
        val teamID = UUID.fromString(auth.name)
        val id = competitionService.start(teamID)
        return ResponseEntity.created(URI.create("/competition/$id")).build()
    }

    @Operation(summary = "Joins a competition",
        description = "Joins a competition.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "competition team may join and the competition starts."),
        ApiResponse(responseCode = "400", description = "token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "user not authorized, token is wrong", content = [Content()]),
        ApiResponse(responseCode = "404", description = "team not existing", content = [Content()]),
        ApiResponse(responseCode = "412", description = "team has no team members", content = [Content()]),
    ]
    )
    @PostMapping("/{id}/join")
    fun join(auth: Authentication,
             @PathVariable(value = "id") id: UUID
    ) : ResponseEntity<Unit> {
        val teamID = UUID.fromString(auth.name)
        val id = competitionService.join(id, teamID)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Stops a competition",
        description = "Stops a competition. One of the teams may stop the competition.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "the competition is stopped"),
        ApiResponse(responseCode = "400", description = "token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "user not authorized, token is wrong", content = [Content()]),
        ApiResponse(responseCode = "404", description = "team not existing or in training", content = [Content()]),
    ]
    )
    @DeleteMapping("/{id}")
    fun stop(auth: Authentication,
             @PathVariable(value = "id") id: UUID) : ResponseEntity<Unit> {
        val teamID = UUID.fromString(auth.name)
        competitionService.stop(id,teamID)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Performs an action",
        description = "Performs any action of the user targeting any player and returns the action result. The training needs to be started once before.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "the result of the action", content = [
            (Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(description = "the result", implementation = ActionResultDto::class)
            ))]),
        ApiResponse(responseCode = "400", description = "token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "user not authorized, token is wrong", content = [Content()]),
        ApiResponse(responseCode = "404", description = "team not existing", content = [Content()]),
        ApiResponse(responseCode = "412", description = "team is not in training, training not started", content = [Content()]),
    ]
    )
    @PostMapping("/{competitionID}/user/{userID}/action/{action}/target/{targetID}")
    fun action(auth: Authentication,
               @PathVariable(value = "competitionID") competitionID: UUID,
               @PathVariable(value = "userID") userID: UUID,
               @PathVariable(value = "action") actionString: String,
               @PathVariable(value = "targetID") targetID: UUID
    ) : ResponseEntity<ActionResultDto> {
        val action = Action.valueOf(actionString)
        val teamID = UUID.fromString(auth.name)
        return ResponseEntity.ok(competitionService.action(competitionID, teamID, userID, action, targetID))
    }
}