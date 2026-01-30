package xyz.engsmyre.moviedescriptiongame.service

import org.springframework.stereotype.Service
import xyz.engsmyre.moviedescriptiongame.db.repository.ChallengeRepository
import xyz.engsmyre.moviedescriptiongame.db.repository.ScoreRepository
import java.time.LocalDate

@Service
class ClueService(
    private val challengeRepository: ChallengeRepository,
    private val scoreRepository: ScoreRepository
    ) {

    fun getClueAndLowerScore(session: String, clueType: String): String {
        val today = LocalDate.now()
        val challengeEntity = challengeRepository.getChallenge(today)!! // TODO Nullcheck
        scoreRepository.appendGivenClue(session, clueType)
        // TODO Get from clueType instead
        return challengeEntity.movies.first().billings[0]!!
    }

}