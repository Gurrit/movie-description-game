package xyz.engsmyre.moviedescriptiongame.challenge

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.common.ApiException
import xyz.engsmyre.moviedescriptiongame.config.GenerationProperties
import java.security.MessageDigest
import java.time.Clock
import java.time.LocalDate

data class GenerateResponse(val date: LocalDate, val tmdbIds: List<Int>)

/** Operational endpoint, documented in api/internal.yaml. Not part of the public contract. */
@RestController
@RequestMapping("/internal")
class InternalGenerationController(
    private val generator: GameGenerator,
    private val props: GenerationProperties,
    private val clock: Clock,
) {
    @PostMapping("/generate")
    suspend fun generate(
        @RequestHeader("X-Internal-Token", required = false) token: String?,
        @RequestParam("date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
    ): GenerateResponse {
        if (props.internalToken.isBlank() || token == null || !constantTimeEquals(token, props.internalToken)) {
            throw ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "Missing or wrong internal token")
        }
        val today = LocalDate.now(clock)
        val target = date ?: today
        if (target.isAfter(today.plusDays(props.preGenerateDays.toLong()))) {
            throw ApiException(
                HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_FAILED", "Date not allowed",
                "Games can be generated at most ${props.preGenerateDays} day(s) ahead",
            )
        }
        val game = generator.ensure(target)
        return GenerateResponse(game.date, game.movies.map { it.id })
    }

    private fun constantTimeEquals(a: String, b: String) =
        MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))
}
