package eu.boxwork.dhbw.capturethecode.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/competition")
class CompetitionControllerOld(
    @Autowired val service: CompetitionService
    ) {
/*
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
        return ResponseEntity.ok(service.getScore(id))
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
        val id = service.start(teamID)
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
        val id = service.join(id, teamID)
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
        service.stop(id,teamID)
        return ResponseEntity.noContent().build()
    }


    @Operation(summary = "Check for flag",
        description = "Checks if a player has the flag. The training needs to be started once before.")
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
    @GetMapping("/user/{userID}")
    fun hasFlag(auth: Authentication,
                @PathVariable(value = "userID") userID: UUID,
    ) : ResponseEntity<Boolean> {
        val teamID = UUID.fromString(auth.name)
        return ResponseEntity.ok(service.checkForFlag(teamID, userID))
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
    @PostMapping("/user/{userID}/action/{action}")
    fun actionNoTarget(auth: Authentication,
                       @PathVariable(value = "userID") userID: UUID,
                       @PathVariable(value = "action") actionString: String
    ) : ResponseEntity<ActionResultDto> {
        val action = Action.valueOf(actionString)
        val teamID = UUID.fromString(auth.name)
        return ResponseEntity.ok(service.action(teamID, userID, action))
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
        return ResponseEntity.ok(service.action(teamID, userID, action, targetID))
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
    }*/
}