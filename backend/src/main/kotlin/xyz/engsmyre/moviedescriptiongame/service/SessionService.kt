package xyz.engsmyre.moviedescriptiongame.service

import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import xyz.engsmyre.moviedescriptiongame.db.entity.SessionEntity
import xyz.engsmyre.moviedescriptiongame.db.repository.SessionRepository
import java.time.LocalDate
import java.util.UUID

@Service
class SessionService(
    private val sessionRepository: SessionRepository,
    private val challengeService: ChallengeService
) {

    fun createSession(): SessionEntity {
        println("GOT HERE")
        val challenge = challengeService.getOrCreateToday()
        val sessionId = UUID.randomUUID().toString()
        return sessionRepository.createSession(
            sessionId = sessionId,
            challengeId = challenge.id,
            challengeDate = challenge.day
        )
    }

    fun getOrCreateSession(sessionId: String?): SessionEntity {
        return if (sessionId != null) {
            sessionRepository.getSession(sessionId) ?: createSession()
        } else {
            createSession()
        }
    }

    fun getSession(sessionId: String): SessionEntity? {
        return sessionRepository.getSession(sessionId)
    }

    fun updateSession(session: SessionEntity): SessionEntity {
        return sessionRepository.updateSession(session)
    }

    fun getSessionByChallenge(challengeId: ObjectId): List<SessionEntity> {
        return sessionRepository.getSessionByChallenge(challengeId)
    }

    fun getSessionByDate(date: LocalDate): List<SessionEntity> {
        val challenge = challengeService.getByDate(date) ?: return emptyList()
        return sessionRepository.getSessionByChallenge(challenge.id)
    }
}
