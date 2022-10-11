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
@RequestMapping("/training")
class TrainingController(
    @Autowired val trainingService: TrainingService
) : AbstractGameController(trainingService) {

    @Operation(summary = "Perform turn",
        description = "Performs a new turn, automatic rounds are not enabled here.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "round performed"),
        ApiResponse(responseCode = "400", description = "token not set", content = [Content()]),
        ApiResponse(responseCode = "401", description = "user not allowed at all", content = [Content()]),
        ApiResponse(responseCode = "403", description = "user not authorized, token is wrong", content = [Content()]),
        ApiResponse(responseCode = "404", description = "team not existing", content = [Content()]),
        ApiResponse(responseCode = "412", description = "team has no team members", content = [Content()]),
    ]
    )
    @PostMapping("/{groundID}/turn")
    fun performTurn(auth: Authentication,
                    @PathVariable(value = "groundID") groundID: UUID,) : ResponseEntity<Unit> {
        val teamID = UUID.fromString(auth.name)
        trainingService.turn(groundID, teamID)
        return ResponseEntity.noContent().build()
    }
}