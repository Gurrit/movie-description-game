package xyz.engsmyre.moviedescriptiongame.db.repository.mongo

import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import xyz.engsmyre.moviedescriptiongame.db.entity.SessionEntity
import xyz.engsmyre.moviedescriptiongame.db.repository.SessionRepository

@Component
class MongoSessionRepository @Autowired constructor(
    private val mongoTemplate: MongoTemplate
) : SessionRepository {

    override fun createSession(sessionId: String, challengeId: ObjectId, challengeDate: String): SessionEntity {
        val session = SessionEntity(
            sessionId = sessionId,
            challengeId = challengeId,
            challengeDate = challengeDate
        )
        return mongoTemplate.insert(session)
    }

    override fun getSession(sessionId: String): SessionEntity? {
        return mongoTemplate.findOne(
            Query.query(Criteria.where(SessionEntity.SESSION_ID_KEY).`is`(sessionId)),
            SessionEntity::class.java
        )
    }

    override fun updateSession(session: SessionEntity): SessionEntity {
        session.updatedAt = java.time.Instant.now()
        mongoTemplate.save(session)
        return session
    }

    override fun getSessionByChallenge(challengeId: ObjectId): List<SessionEntity> {
        return mongoTemplate.find(
            Query.query(Criteria.where(SessionEntity.CHALLENGE_ID_KEY).`is`(challengeId)),
            SessionEntity::class.java
        )
    }
}
