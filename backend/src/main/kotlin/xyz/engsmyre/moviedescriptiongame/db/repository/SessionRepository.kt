package xyz.engsmyre.moviedescriptiongame.db.repository

import org.bson.types.ObjectId
import xyz.engsmyre.moviedescriptiongame.db.entity.SessionEntity

interface SessionRepository {
    fun createSession(sessionId: String, challengeId: ObjectId, challengeDate: String): SessionEntity
    fun getSession(sessionId: String): SessionEntity?
    fun updateSession(session: SessionEntity): SessionEntity
    fun getSessionByChallenge(challengeId: ObjectId): List<SessionEntity>
}
