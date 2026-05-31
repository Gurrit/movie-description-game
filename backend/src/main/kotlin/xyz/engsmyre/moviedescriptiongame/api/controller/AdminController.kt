package xyz.engsmyre.moviedescriptiongame.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.api.dto.ChallengeResponse
import xyz.engsmyre.moviedescriptiongame.api.mapper.ChallengeMapper
import xyz.engsmyre.moviedescriptiongame.service.AdminService

@Tag(name = "Admin", description = "Administrative operations")
@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminService: AdminService,
    private val challengeMapper: ChallengeMapper
) {

    @Operation(
        summary = "Regenerate today's challenge",
        description = "Force regeneration of today's movie challenge (admin only)"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Newly generated challenge"),
            ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(mediaType = "application/json")]),
            ApiResponse(responseCode = "500", description = "Failed to generate challenge", content = [Content(mediaType = "application/json")])
        ]
    )
    @PostMapping("/refresh")
    fun refreshChallenge(): ResponseEntity<ChallengeResponse> {
        val challenge = adminService.updateCurrentChallenge()
        return ResponseEntity.ok(challengeMapper.toResponse(challenge))
    }
}
