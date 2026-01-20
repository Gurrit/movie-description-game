package xyz.engsmyre.moviedescriptiongame.config

import com.mongodb.client.MongoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AppConfig {

    @Bean fun mongoClient(): MongoClient {
        return com.mongodb.client.MongoClients.create("mongodb://root:example@0.0.0.0:27017?directConnection=true")
    }
    }