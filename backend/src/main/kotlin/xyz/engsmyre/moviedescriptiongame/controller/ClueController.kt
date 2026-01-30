package xyz.engsmyre.moviedescriptiongame.controller

import org.apache.coyote.Response
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.service.ClueService

@RestController
@RequestMapping("/clue")
class ClueController(
    val clueService: ClueService
) {

    // TODO Include Session in request.
    @PostMapping("/{type}")
    fun getClue(type: String): ResponseEntity<String> {  // TODO Should probably return something else.
        val clue = clueService.getClueAndLowerScore(type)
        return ResponseEntity.ok(clue)
    }


}