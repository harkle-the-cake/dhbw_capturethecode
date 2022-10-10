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
import java.util.*

@RestController
@RequestMapping("/training")
class TrainingController(
    @Autowired val trainingService: TrainingService
) {
    @Operation(summary = "training result",
        description = "returns the current training scores, will increase each time frame if flag not lost.")
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
    @GetMapping("")
    fun getScore(auth: Authentication) : ResponseEntity<ScoreDto> {
        val teamID = UUID.fromString(auth.name)
        return ResponseEntity.ok(trainingService.getScore(teamID))
    }

    @Operation(summary = "Start a training",
        description = "Starts a new training.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "training is started all states are reset"),
        ApiResponse(responseCode = "400", description = "token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "user not authorized, token is wrong", content = [Content()]),
        ApiResponse(responseCode = "404", description = "team not existing", content = [Content()]),
        ApiResponse(responseCode = "412", description = "team has no team members", content = [Content()]),
    ]
    )
    @PostMapping("")
    fun start(auth: Authentication) : ResponseEntity<Unit> {
        val teamID = UUID.fromString(auth.name)
        trainingService.start(teamID)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Stops a training",
        description = "Stops a training.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "the training is stopped"),
        ApiResponse(responseCode = "400", description = "token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "user not authorized, token is wrong", content = [Content()]),
        ApiResponse(responseCode = "404", description = "team not existing or in training", content = [Content()]),
    ]
    )
    @DeleteMapping("")
    fun stop(auth: Authentication) : ResponseEntity<Unit> {
        val teamID = UUID.fromString(auth.name)
        trainingService.stop(teamID)
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
    @PostMapping("/user/{userID}/action/{action}/target/{targetID}")
    fun action(auth: Authentication,
               @PathVariable(value = "userID") userID: UUID,
               @PathVariable(value = "action") actionString: String,
               @PathVariable(value = "targetID") targetID: UUID
    ) : ResponseEntity<ActionResultDto> {
        val action = Action.valueOf(actionString)
        val teamID = UUID.fromString(auth.name)
        return ResponseEntity.ok(trainingService.action(teamID, userID, action, targetID))
    }
}