package eu.boxwork.dhbw.capturethecode.service

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
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/team")
class TeamController(
    @Autowired val teamService: TeamService,
    @Autowired val playerService: PlayerService
) {
    @Operation(summary = "List all teams",
        description = "List all existing teams.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "teams are available", content = [
            (Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(description = "list of teams as JSON array", implementation = Team::class)
            ))]),
        ApiResponse(responseCode = "400", description = "Bad request: e.g. user id not valid", content = [Content()]),
        ApiResponse(responseCode = "401", description = "Not authenticated: user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "Not authorized: user not allowed to get infos", content = [Content()])]
    )
    @GetMapping("")
    fun getTeams(
    ) : ResponseEntity<MutableList<TeamDto>> {
        return ResponseEntity.ok(teamService.list())
    }

    @Operation(summary = "Get a team by ID",
        description = "Get a team by id. Also adds the team members")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "team is available", content = [
            (Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(description = "the team with members", implementation = TeamWithMembersDto::class)
            ))]),
        ApiResponse(responseCode = "400", description = "Bad request: e.g. team id not valid", content = [Content()]),
        ApiResponse(responseCode = "401", description = "Not authenticated: user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "404", description = "Not found: team is not found", content = [Content()])]
    )
    @GetMapping("/{id}")
    fun getTeam(
                 @PathVariable(value = "id") id: UUID
    ) : ResponseEntity<TeamWithMembersDto> {
       val team = teamService.get(uuid = id)?:return ResponseEntity.notFound().build()
       val players = playerService.findByTeam(id)
       val ret = TeamWithMembersDto(
           team.uuid,
           team.teamName,
           players
       )
        return ResponseEntity.ok(ret)
    }

    @Operation(summary = "Adds a team",
        description = "Adds a new team, returns the UUID of the new team")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "team is created", content = [
            (Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(description = "the team UUID", implementation = UUID::class)
            ))]),
        ApiResponse(responseCode = "400", description = "team name not valid, token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "409", description = "team name already exists", content = [Content()]),
        ApiResponse(responseCode = "418", description = "max team count reached", content = [Content()])]
    )
    @PutMapping("")
    fun addTeam(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description =
        "the new team to add.", required = true, content = [Content(
        schema = Schema(implementation = TeamDto::class))]) @Valid @RequestBody teamDto: TeamDto
    ) : ResponseEntity<UUID> {
        val ret = teamService.add(teamDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(ret.uuid)
    }

    @Operation(summary = "Changes a team",
        description = "Changes a team, returns the new team value. Team token must be sent in the header field 'Authorization'.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "team is changed", content = [
            (Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(description = "the team", implementation = TeamDto::class)
            ))]),
        ApiResponse(responseCode = "400", description = "team name not valid, token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "user not authorized, token is wrong", content = [Content()]),
        ApiResponse(responseCode = "404", description = "team not existing", content = [Content()]),
        ApiResponse(responseCode = "418", description = "max team count reached", content = [Content()])]
    )
    @PostMapping("/{id}")
    fun changeTeam(
                   @PathVariable(value = "id") id: UUID,
                   @RequestHeader(value = "Authorization") token: String,
                @io.swagger.v3.oas.annotations.parameters.RequestBody(description =
                "the team infos to set.", required = true, content = [Content(
                    schema = Schema(implementation = TeamDto::class))]) @Valid @RequestBody teamDto: TeamDto
    ) : ResponseEntity<TeamDto> {
        val tokenCleaned = token.replace("token","").trim()
        return ResponseEntity.ok(teamService.change(tokenCleaned, id, teamDto))
    }

    @Operation(summary = "Deletes a team",
        description = "Deletes a team. Team token must be sent in the header field 'Authorization'.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "team is deleted", content = [Content()]),
        ApiResponse(responseCode = "400", description = "team name not valid, token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "user not authorized, token is wrong", content = [Content()]),
        ApiResponse(responseCode = "404", description = "team not existing", content = [Content()])
    ]
    )
    @DeleteMapping("/{id}")
    fun deleteTeam(
        @PathVariable(value = "id") id: UUID
    ) : ResponseEntity<TeamDto> {
        teamService.delete(id)
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