package xyz.engsmyre.moviedescriptiongame.db.repository.mongo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import xyz.engsmyre.moviedescriptiongame.db.entity.ChallengeEntity
import xyz.engsmyre.moviedescriptiongame.db.repository.ChallengeRepository
import java.time.LocalDate

@Component
class MongoChallengeRepository @Autowired constructor(val mongoTemplate: MongoTemplate): ChallengeRepository {

    override fun insertChallenge(challengeEntity: ChallengeEntity): ChallengeEntity {
        return mongoTemplate.insert(challengeEntity)
    }

    override fun getChallenge(day: LocalDate): ChallengeEntity? {
        return mongoTemplate.find(
            Query.query(Criteria.where(ChallengeEntity.DAY_KEY).`is`(day.toString())),
            ChallengeEntity::class.java
        ).firstOrNull()
    }
}