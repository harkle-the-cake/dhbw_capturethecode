package eu.boxwork.dhbw.capturethecode.service

import eu.boxwork.dhbw.capturethecode.dto.ActionResultDto
import eu.boxwork.dhbw.capturethecode.dto.ScoreDto
import eu.boxwork.dhbw.capturethecode.enums.Action
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.hibernate.exception.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*

open class AbstractGameController(
    val service: AbstractGameService
) {
    @Operation(summary = "get result",
        description = "returns the current scores, will increase each time frame if flag not lost.")
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
    @GetMapping("/{groundID}")
    fun getScore(auth: Authentication,
        @PathVariable(value = "groundID") groundID: UUID,) : ResponseEntity<ScoreDto> {
        val teamID = UUID.fromString(auth.name)
        return ResponseEntity.ok(service.getScore(groundID,teamID))
    }

    @Operation(summary = "Start a game",
        description = "Starts a new game.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "game is started all states are reset, the UUID of the ground is returned"),
        ApiResponse(responseCode = "400", description = "token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "user not authorized, token is wrong", content = [Content()]),
        ApiResponse(responseCode = "404", description = "team not existing", content = [Content()]),
        ApiResponse(responseCode = "412", description = "team has no team members", content = [Content()]),
    ]
    )
    @PutMapping("")
    fun start(auth: Authentication) : ResponseEntity<UUID> {
        val teamID = UUID.fromString(auth.name)
        val groundID = service.start(teamID)
        return ResponseEntity.ok(groundID)
    }

    @Operation(summary = "Stops a game",
        description = "Stops a game.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "the game is stopped"),
        ApiResponse(responseCode = "400", description = "token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "user not authorized, token is wrong", content = [Content()]),
        ApiResponse(responseCode = "404", description = "team not existing or in training", content = [Content()]),
    ]
    )
    @DeleteMapping("/{groundID}")
    fun stop(auth: Authentication,
             @PathVariable(value = "groundID") groundID: UUID,
    ) : ResponseEntity<Unit> {
        val teamID = UUID.fromString(auth.name)
        service.stop(groundID, teamID)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Check for flag",
        description = "Checks if a player has the flag. The game needs to be started once before.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "the player check is ok", content = [
            (Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(description = "true, if the player has the flag, else false", implementation = Boolean::class)
            ))]),
        ApiResponse(responseCode = "400", description = "token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "user not authorized, token is wrong", content = [Content()]),
        ApiResponse(responseCode = "404", description = "team not existing", content = [Content()]),
        ApiResponse(responseCode = "412", description = "team is not in training, training not started", content = [Content()]),
    ]
    )
    @GetMapping("/{groundID}/user/{userID}")
    fun hasFlag(auth: Authentication,
               @PathVariable(value = "groundID") groundID: UUID,
               @PathVariable(value = "userID") userID: UUID,
    ) : ResponseEntity<Boolean> {
        val teamID = UUID.fromString(auth.name)
        return ResponseEntity.ok(service.checkForFlag(groundID, teamID, userID))
    }

    @Operation(summary = "Performs an action (no target)",
        description = "Performs any action of the user targeting no player and returns the action result. The training needs to be started once before.")
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
    @PostMapping("/{groundID}/user/{userID}/action/{action}")
    fun actionNoTarget(auth: Authentication,
               @PathVariable(value = "groundID") groundID: UUID,
               @PathVariable(value = "userID") userID: UUID,
               @PathVariable(value = "action") actionString: String
    ) : ResponseEntity<ActionResultDto> {
        val action = Action.valueOf(actionString)
        val teamID = UUID.fromString(auth.name)
        return ResponseEntity.ok(service.action(groundID, teamID, userID, action))
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
    @PostMapping("/{groundID}/user/{userID}/action/{action}/target/{targetID}")
    fun action(auth: Authentication,
               @PathVariable(value = "groundID") groundID: UUID,
               @PathVariable(value = "userID") userID: UUID,
               @PathVariable(value = "action") actionString: String,
               @PathVariable(value = "targetID") targetID: UUID
    ) : ResponseEntity<ActionResultDto> {
        val action = Action.valueOf(actionString)
        val teamID = UUID.fromString(auth.name)
        return ResponseEntity.ok(service.action(groundID, teamID, userID, action, targetID))
    }

    /*
    * ###########################
    * EXCEPTION HANDLER
    * */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleException(ex: ConstraintViolationException) : ResponseEntity<String>{
        return  ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ServiceException::class)
    fun handleException(ex:ServiceException) : ResponseEntity<String>{
        return ResponseEntity.status(ex.code).body(ex.message)
    }

    @ExceptionHandler(ConcurrentModificationException::class)
    fun handleException(ex:ConcurrentModificationException) : ResponseEntity<String>{
        ex.printStackTrace()
        return ResponseEntity.status(HttpStatus.LOCKED).body("access is locked, retry")
    }
}