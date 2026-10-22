package xyz.engsmyre.moviedescriptiongame.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("tmdb")
data class TmdbProperties(
    val baseUrl: String = "https://api.themoviedb.org/3",
    val readToken: String = "",
    val minVoteCount: Int = 5000,
    val minRuntime: Int = 60,
    val excludedGenres: List<Int> = listOf(99),
    val originalLanguage: String? = null,
    val maxPoolPages: Int = 100,
    val includeAdult: Boolean = false,
) {
    init {
        require(minVoteCount >= 0) { "tmdb.min-vote-count must be >= 0" }
        require(maxPoolPages in 1..500) { "tmdb.max-pool-pages must be in 1..500" }
    }
}
