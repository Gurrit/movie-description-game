package xyz.engsmyre.moviedescriptiongame.db.repository

import xyz.engsmyre.moviedescriptiongame.db.entity.ChallengeEntity
import java.time.LocalDate


interface ChallengeRepository {
    fun insertChallenge(challengeEntity: ChallengeEntity): ChallengeEntity
    fun getChallenge(day: LocalDate): ChallengeEntity?

}