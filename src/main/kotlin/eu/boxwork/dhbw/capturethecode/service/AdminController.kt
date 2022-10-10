package eu.boxwork.dhbw.capturethecode.service

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/admin")
class AdminController(
    @Autowired val adminService: AdminService
) {
    @Operation(summary = "clears all",
        description = "clears all teams, players, trainings, competitions.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "all cleared"),
        ApiResponse(responseCode = "401", description = "Not authenticated: user not allowed at all", content = [Content()])
    ]
    )
    @DeleteMapping("/clear")
    fun clear(
    ) : ResponseEntity<Unit> {
        adminService.clear()
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "clears trainings",
        description = "clears all trainings.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "all trainings cleared"),
        ApiResponse(responseCode = "401", description = "Not authenticated: user not allowed at all", content = [Content()])
    ]
    )
    @DeleteMapping("/trainings")
    fun clearTrainings(
    ) : ResponseEntity<Unit> {
        adminService.clearTrainings()
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "clears competitions",
        description = "clears all competitions.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "all competitions cleared"),
        ApiResponse(responseCode = "401", description = "Not authenticated: user not allowed at all", content = [Content()])
    ]
    )
    @DeleteMapping("/competitions")
    fun clearCompetitions(
    ) : ResponseEntity<Unit> {
        adminService.clearCompetitions()
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "removes a team",
        description = "removes a team.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "team removed"),
        ApiResponse(responseCode = "401", description = "Not authenticated: user not allowed at all")
    ]
    )
    @DeleteMapping("/team/{id}")
    fun deleteTeam(
        @PathVariable(value = "id") id: UUID
    ) : ResponseEntity<Unit> {
        adminService.deleteTeam(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "removes the team members",
        description = "removes a team.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "members are cleared"),
        ApiResponse(responseCode = "401", description = "Not authenticated: user not allowed at all")
        ]
    )
    @DeleteMapping("/team/{id}/members")
    fun clearMembers(
        @PathVariable(value = "id") id: UUID
    ) : ResponseEntity<Unit> {
        adminService.deleteMembers(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "removes a player",
        description = "removes a player.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "player is deleted"),
        ApiResponse(responseCode = "401", description = "Not authenticated: user not allowed at all")
    ]
    )
    @DeleteMapping("/player/{id}")
    fun deletePlayer(
        @PathVariable(value = "id") id: UUID
    ) : ResponseEntity<Unit> {
        adminService.deletePlayer(id)
        return ResponseEntity.noContent().build()
    }
}