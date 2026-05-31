package xyz.engsmyre.moviedescriptiongame.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.api.dto.ChallengeResponse
import xyz.engsmyre.moviedescriptiongame.api.dto.ErrorResponse
import xyz.engsmyre.moviedescriptiongame.api.mapper.ChallengeMapper
import xyz.engsmyre.moviedescriptiongame.service.ChallengeService
import java.time.LocalDate

@Tag(name = "Challenge", description = "Daily movie challenge operations")
@RestController
@RequestMapping("/challenge")
class ChallengeController(
    private val challengeService: ChallengeService,
    private val challengeMapper: ChallengeMapper
) {

    @GetMapping("/today")
    @Operation(
        summary = "Get today's challenge",
        description = "Returns the current day's movie challenge with 5 movies"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Today's challenge"),
            ApiResponse(responseCode = "404", description = "No challenge exists for today yet", content = [Content(mediaType = "application/json")]),
            ApiResponse(responseCode = "500", description = "Internal server error", content = [Content(mediaType = "application/json")])
        ]
    )
    fun getTodayChallenge(): ResponseEntity<ChallengeResponse> {
        val challenge = challengeService.getOrCreateToday()
        return ResponseEntity.ok(challengeMapper.toResponse(challenge))
    }

    @Operation(
        summary = "Get challenge for a specific date",
        description = "Returns the movie challenge for the specified date"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Challenge for the specified date"),
            ApiResponse(responseCode = "400", description = "Invalid date format", content = [Content(mediaType = "application/json")]),
            ApiResponse(responseCode = "404", description = "No challenge exists for the specified date", content = [Content(mediaType = "application/json")])
        ]
    )
    @GetMapping("/{date}")
    fun getChallengeByDate(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @PathVariable
        date: LocalDate
    ): ResponseEntity<ChallengeResponse> {
        val challenge = challengeService.getByDate(date)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(challengeMapper.toResponse(challenge))
    }
}
