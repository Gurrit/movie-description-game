package xyz.engsmyre.moviedescriptiongame.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.api.dto.GuessRequest
import xyz.engsmyre.moviedescriptiongame.api.dto.GuessResponse
import xyz.engsmyre.moviedescriptiongame.service.GuessService

@Tag(name = "Guess", description = "Submit guesses and get scores")
@RestController
@RequestMapping("/guess")
class GuessController(
    private val guessService: GuessService
) {

    @Operation(
        summary = "Submit a movie guess",
        description = "Validates the guess against the current challenge's movies. Returns whether the guess is correct and the score earned."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Guess result and updated score"),
            ApiResponse(responseCode = "400", description = "Invalid request (missing fields)", content = [Content(mediaType = "application/json")]),
            ApiResponse(responseCode = "404", description = "Session or challenge not found", content = [Content(mediaType = "application/json")])
        ]
    )
    @PostMapping()
    fun submitGuess(
        @RequestBody
        guessRequest: GuessRequest
    ): ResponseEntity<GuessResponse> {
        val response = guessService.submitGuess(guessRequest.sessionId, guessRequest.movieId)
        return ResponseEntity.ok(response)
    }
}
