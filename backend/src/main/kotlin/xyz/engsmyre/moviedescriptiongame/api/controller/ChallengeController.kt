package xyz.engsmyre.moviedescriptiongame.api.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.api.generated.ChallengeApi
import xyz.engsmyre.moviedescriptiongame.api.mapper.ChallengeMapper
import xyz.engsmyre.moviedescriptiongame.api.model.ChallengeResponse
import xyz.engsmyre.moviedescriptiongame.service.ChallengeService
import java.time.LocalDate

@RestController
class ChallengeController(
    private val challengeService: ChallengeService,
    private val challengeMapper: ChallengeMapper
) : ChallengeApi {

    override suspend fun getTodayChallenge(): ResponseEntity<ChallengeResponse> {
        val challenge = withContext(Dispatchers.IO) { challengeService.getOrCreateToday() }
        return ResponseEntity.ok(challengeMapper.toResponse(challenge))
    }

    override suspend fun getChallengeByDate(date: LocalDate): ResponseEntity<ChallengeResponse> {
        val challenge = withContext(Dispatchers.IO) { challengeService.getByDate(date) }
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(challengeMapper.toResponse(challenge))
    }
}
