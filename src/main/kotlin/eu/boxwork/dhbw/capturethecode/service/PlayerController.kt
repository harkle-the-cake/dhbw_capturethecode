package eu.boxwork.dhbw.capturethecode.service

import eu.boxwork.dhbw.capturethecode.dto.PlayerDto
import eu.boxwork.dhbw.capturethecode.dto.TeamDto
import eu.boxwork.dhbw.capturethecode.dto.TeamWithMembersDto
import eu.boxwork.dhbw.capturethecode.model.Team
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
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/player")
@Validated
class PlayerController(
    @Autowired val playerService: PlayerService
) {
    @Operation(summary = "List all players",
        description = "List all existing players.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "players are available", content = [
            (Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(description = "list of player as JSON array", implementation = Team::class)
            ))]),
        ApiResponse(responseCode = "400", description = "Bad request: e.g. user id not valid", content = [Content()]),
        ApiResponse(responseCode = "401", description = "Not authenticated: user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "Not authorized: user not allowed to get infos", content = [Content()])]
    )
    @GetMapping("")
    fun getPlayer(
    ) : ResponseEntity<MutableList<PlayerDto>> {
        return ResponseEntity.ok(playerService.list())
    }

    @Operation(summary = "List team players",
        description = "List all existing players for a team.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "players are available", content = [
            (Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(description = "list of player as JSON array", implementation = Team::class)
            ))]),
        ApiResponse(responseCode = "400", description = "Bad request: e.g. user id not valid", content = [Content()]),
        ApiResponse(responseCode = "401", description = "Not authenticated: user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "Not authorized: user not allowed to get infos", content = [Content()])]
    )
    @GetMapping("/team/{id}")
    fun getTeamsPlayers(
        @PathVariable(value = "id") id: UUID
    ) : ResponseEntity<MutableList<PlayerDto>> {
        return ResponseEntity.ok(playerService.findByTeam(id))
    }

    @Operation(summary = "Get a player by ID",
        description = "Get a player by id.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "player is available", content = [
            (Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(description = "the player", implementation = PlayerDto::class)
            ))]),
        ApiResponse(responseCode = "400", description = "Bad request: e.g. player id not valid", content = [Content()]),
        ApiResponse(responseCode = "401", description = "Not authenticated: user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "404", description = "Not found: player is not found", content = [Content()])]
    )
    @GetMapping("/{id}")
    fun getPlayerByID(
                 @PathVariable(value = "id") id: UUID
    ) : ResponseEntity<PlayerDto> {
       val player = playerService.get(uuid = id)?:return ResponseEntity.notFound().build()
       return ResponseEntity.ok(player)
    }

    @Operation(summary = "Adds a player",
        description = "Adds a new player, returns the UUID of the new player")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "player is created", content = [
            (Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(description = "the player UUID", implementation = UUID::class)
            ))]),
        ApiResponse(responseCode = "400", description = "player name not valid", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "409", description = "player name already exists", content = [Content()]),
        ApiResponse(responseCode = "418", description = "max player count reached", content = [Content()])]
    )
    @PutMapping("")
    fun addPlayer(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description =
        "the new player to add.", required = true, content = [Content(
        schema = Schema(implementation = PlayerDto::class))]) @Valid @RequestBody player: PlayerDto
    ) : ResponseEntity<UUID> {
        val ret = playerService.add(player)
        return ResponseEntity.status(HttpStatus.CREATED).body(ret.uuid)
    }

    @Operation(summary = "Changes a player",
        description = "Changes a player, returns the new player value. Team token must be sent in the header field 'Authorization'.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "player is changed", content = [
            (Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(description = "the team", implementation = TeamDto::class)
            ))]),
        ApiResponse(responseCode = "400", description = "player name not valid, token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "user not authorized, token is wrong", content = [Content()]),
        ApiResponse(responseCode = "404", description = "player not existing", content = [Content()]),
    ]
    )
    @PostMapping("/{id}")
    fun changePlayer(
                   @PathVariable(value = "id") id: UUID,
                   @RequestHeader(value = "Authorization") token: String,
                @io.swagger.v3.oas.annotations.parameters.RequestBody(description =
                "the team infos to set.", required = true, content = [Content(
                    schema = Schema(implementation = TeamDto::class))]) @Valid @RequestBody player: PlayerDto
    ) : ResponseEntity<PlayerDto> {
        val tokenCleaned = token.replace("token","").trim()
        return ResponseEntity.ok(playerService.change(tokenCleaned, id, player))
    }

    @Operation(summary = "Deletes a player",
        description = "Deletes a player. Team token must be sent in the header field 'Authorization'.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "player is deleted", content = [Content()]),
        ApiResponse(responseCode = "400", description = "player ID not valid, token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "user not authorized, token is wrong", content = [Content()]),
        ApiResponse(responseCode = "404", description = "player not existing", content = [Content()])
    ]
    )
    @DeleteMapping("/{id}")
    fun deletePlayer(
                   @PathVariable(value = "id") id: UUID,
                   @RequestHeader(value = "Authorization") token: String
    ) : ResponseEntity<Unit> {
        val tokenCleaned = token.replace("token","").trim()
        playerService.delete(tokenCleaned, id)
        return ResponseEntity.noContent().build()
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
}