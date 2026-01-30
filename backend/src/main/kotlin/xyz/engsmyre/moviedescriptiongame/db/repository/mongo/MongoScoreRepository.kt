package xyz.engsmyre.moviedescriptiongame.db.repository.mongo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Score
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.BasicUpdate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import xyz.engsmyre.moviedescriptiongame.db.entity.ScoreEntity
import xyz.engsmyre.moviedescriptiongame.db.repository.ScoreRepository

@Component
class MongoScoreRepository @Autowired constructor(val mongoTemplate: MongoTemplate): ScoreRepository {
    override fun appendGivenClue(sessionId: String, clue: String): ScoreEntity {
        mongoTemplate.updateFirst(Query.query(
            Criteria.where(ScoreEntity.SESSION_ID_KEY).`is`(sessionId)),
            // TODO This is wrong, should take which movies the clue is for into account
            BasicUpdate.update(ScoreEntity.GIVEN_CLUES_KEY, clue).push(ScoreEntity.GIVEN_CLUES_KEY, clue),
            ScoreEntity::class.java)

        return getCurrentScoreEntity(sessionId)
    }

    override fun getCurrentScoreEntity(sessionId: String): ScoreEntity {
        return mongoTemplate.findOne(
            Query.query(Criteria.where(ScoreEntity.SESSION_ID_KEY).`is`(sessionId)),
        ScoreEntity::class.java
        )!! // TODO Something else
    }


}