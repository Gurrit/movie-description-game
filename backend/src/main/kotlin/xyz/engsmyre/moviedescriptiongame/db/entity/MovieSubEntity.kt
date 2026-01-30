package xyz.engsmyre.moviedescriptiongame.db.entity

import org.springframework.data.mongodb.core.mapping.Field

data class MovieSubEntity (
    @Field(MOVIE_TITLE_KEY)
    var movieTitle: String,
    @Field(MOVIE_ORIGINAL_TITLE_KEY)
    var movieOriginalTitle: String,
    @Field(YEAR_KEY)
    var year: String,
    @Field(DIRECTORS_KEY)
    var directors: List<String>,
    @Field(BILLINGS_KEY)
    var billings: Map<Int, String>,
    @Field(WRITER_KEY) // TODO Or List?
    var writer: String,
    @Field(BUDGET_KEY)
    var budget: Long,
    @Field(COVER_ART_URL_KEY)
    var coverArtUrl: String
) {
    companion object {
        // TODO shouls be enum instead.
        const val MOVIE_TITLE_KEY = "movieTitle"
        const val MOVIE_ORIGINAL_TITLE_KEY = "movieOriginalTitle"
        const val YEAR_KEY = "year"
        const val DIRECTORS_KEY = "directors"
        const val BILLINGS_KEY = "billings"
        const val WRITER_KEY = "writer"
        const val BUDGET_KEY = "budget"
        const val COVER_ART_URL_KEY = "coverArtUrl"
    }
}