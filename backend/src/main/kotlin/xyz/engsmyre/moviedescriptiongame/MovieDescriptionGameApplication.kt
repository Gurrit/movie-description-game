package xyz.engsmyre.moviedescriptiongame

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
@EnableMongoRepositories
open class MovieDescriptionGameApplication {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            runApplication<MovieDescriptionGameApplication>()
        }
    }
}