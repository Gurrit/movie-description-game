package xyz.engsmyre.moviedescriptiongame.tmdb.request_config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TmdbMovieClientConfig {

    @Bean("TmdbMovieClient")
    fun tmdbMovieWebClient(): org.springframework.web.reactive.function.client.WebClient {
        return org.springframework.web.reactive.function.client.WebClient.builder()
            .baseUrl("https://api.themoviedb.org/3/movie")
            .build()
    }

    @Bean("TmdbCreditsClient")
    fun tmdbCreditsWebClient(): org.springframework.web.reactive.function.client.WebClient {
        return org.springframework.web.reactive.function.client.WebClient.builder()
            .baseUrl("https://api.themoviedb.org/3/movie")
            .build()
    }
}
