package xyz.engsmyre.moviedescriptiongame.service

import org.springframework.stereotype.Service
import xyz.engsmyre.moviedescriptiongame.db.repository.ChallengeRepository
import java.time.LocalDate

@Service
class ChallengeService(
    private val challengeRepository: ChallengeRepository,
    private val adminService: AdminService
) {

    fun getOrCreateToday(): xyz.engsmyre.moviedescriptiongame.db.entity.ChallengeEntity {
        val today = LocalDate.now()
        return challengeRepository.getChallenge(today) ?: adminService.updateCurrentChallenge()
    }

    fun getByDate(date: LocalDate): xyz.engsmyre.moviedescriptiongame.db.entity.ChallengeEntity? {
        return challengeRepository.getChallenge(date)
    }
}
