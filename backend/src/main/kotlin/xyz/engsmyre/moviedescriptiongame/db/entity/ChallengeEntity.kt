package xyz.engsmyre.moviedescriptiongame.db.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.util.UUID

@Document(collection = "challenge")
// TODO Finish this
data class ChallengeEntity (

    @Id
    var id: UUID,

    @Field
    var movieName: String
)
