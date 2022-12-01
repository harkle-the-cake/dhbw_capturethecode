package eu.boxwork.dhbw.capturethecode.service

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/competition")
class CompetitionController(
    @Autowired val competitionService: CompetitionService
) : AbstractGameController(competitionService) {

    @Operation(summary = "Joins a competition",
        description = "Joins a competition. Competition start immediately after joining.")
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
        competitionService.join(id, teamID)
        return ResponseEntity.ok().build()
    }
}