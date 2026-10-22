package xyz.engsmyre.moviedescriptiongame.api.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.api.generated.AdminApi
import xyz.engsmyre.moviedescriptiongame.api.mapper.ChallengeMapper
import xyz.engsmyre.moviedescriptiongame.api.model.ChallengeResponse
import xyz.engsmyre.moviedescriptiongame.service.AdminService

@RestController
class AdminController(
    private val adminService: AdminService,
    private val challengeMapper: ChallengeMapper
) : AdminApi {

    override suspend fun refreshChallenge(): ResponseEntity<ChallengeResponse> {
        val challenge = withContext(Dispatchers.IO) { adminService.updateCurrentChallenge() }
        return ResponseEntity.ok(challengeMapper.toResponse(challenge))
    }
}
