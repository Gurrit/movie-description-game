package xyz.engsmyre.moviedescriptiongame.game

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.api.generated.GameApi
import xyz.engsmyre.moviedescriptiongame.api.model.Game
import xyz.engsmyre.moviedescriptiongame.api.model.GuessRequest
import xyz.engsmyre.moviedescriptiongame.player.currentPlayerId

@RestController
class GameController(private val service: PlaythroughService) : GameApi {

    override suspend fun getTodaysGame(): ResponseEntity<Game> =
        ResponseEntity.ok(service.getToday(currentPlayerId()))

    override suspend fun startTodaysGame(): ResponseEntity<Game> {
        val started = service.startToday(currentPlayerId())
        return ResponseEntity.status(if (started.created) HttpStatus.CREATED else HttpStatus.OK).body(started.game)
    }

    override suspend fun revealClue(clueId: String): ResponseEntity<Game> =
        ResponseEntity.ok(service.reveal(currentPlayerId(), clueId))

    override suspend fun submitGuess(guessRequest: GuessRequest): ResponseEntity<Game> =
        ResponseEntity.ok(service.guess(currentPlayerId(), guessRequest.tmdbId))
}
