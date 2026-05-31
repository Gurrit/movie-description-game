package xyz.engsmyre.moviedescriptiongame.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.api.dto.ClueRequest
import xyz.engsmyre.moviedescriptiongame.api.dto.ClueResponse
import xyz.engsmyre.moviedescriptiongame.api.dto.ClueType
import xyz.engsmyre.moviedescriptiongame.service.ClueService

@Tag(name = "Clue", description = "Request clues for movies")
@RestController
@RequestMapping("/clue")
class ClueController(
    private val clueService: ClueService
) {

    @Operation(
        summary = "Request a clue",
        description = "Returns a clue of the specified type for the current movie. Deducts points from the session's score based on the clue type."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Clue and updated session state"),
            ApiResponse(responseCode = "400", description = "Invalid clue type or missing session", content = [Content(mediaType = "application/json")]),
            ApiResponse(responseCode = "404", description = "Session not found", content = [Content(mediaType = "application/json")])
        ]
    )
    @PostMapping("/{type}")
    fun getClue(
        @Parameter(description = "Type of clue to request")
        @PathVariable
        type: String,
        @RequestBody
        clueRequest: ClueRequest
    ): ResponseEntity<ClueResponse> {
        try {
            val clueType = ClueType.valueOf(type.uppercase())
            val response = clueService.getClue(clueRequest.sessionId, clueType)
            return ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid clue type: $type. Valid types are: ${ClueType.values().joinToString(", ")}")
        }
    }
}
