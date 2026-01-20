package xyz.engsmyre.moviedescriptiongame.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.db.entity.ChallengeEntity
import xyz.engsmyre.moviedescriptiongame.service.AdminService
import java.time.LocalDate

@RestController
@RequestMapping("/admin")
class AdminController(
    val adminService: AdminService
) {

    @PostMapping("/challenge")
    final fun updateChallenge(): ResponseEntity<ChallengeEntity> { // TODO Response type
        val challenge = adminService.updateCurrentChallenge()
        return ResponseEntity(challenge, HttpStatus.OK)
    }

    @GetMapping("/challenge")
    final fun getCurrentChallenge(): ResponseEntity<ChallengeEntity> {  // TODO Response type
        val challenge = adminService.getChallenge(LocalDate.now())
        // TODO Cleanup
        if (challenge != null) {
            return ResponseEntity(challenge, HttpStatus.OK)
        }
        return ResponseEntity.notFound().build()
    }

}