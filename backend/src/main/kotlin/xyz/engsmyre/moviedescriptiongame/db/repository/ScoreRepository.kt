package xyz.engsmyre.moviedescriptiongame.db.repository

import xyz.engsmyre.moviedescriptiongame.db.entity.ScoreEntity

interface ScoreRepository {
    fun appendGivenClue(sessionId: String, clue: String): ScoreEntity
    fun getCurrentScoreEntity(sessionId: String): ScoreEntity?
}