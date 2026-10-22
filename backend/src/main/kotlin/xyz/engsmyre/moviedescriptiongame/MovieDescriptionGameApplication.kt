package xyz.engsmyre.moviedescriptiongame

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class MovieDescriptionGameApplication

fun main(args: Array<String>) {
    runApplication<MovieDescriptionGameApplication>(*args)
}
