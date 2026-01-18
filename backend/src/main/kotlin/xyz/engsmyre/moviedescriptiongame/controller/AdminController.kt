package xyz.engsmyre.moviedescriptiongame.controller

import org.apache.coyote.Response
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.service.AdminService

@RestController
@RequestMapping("/admin")
class AdminController(
    val adminService: AdminService
) {

    @PostMapping("/challenge")
    final fun updateChallenge(): ResponseEntity<String> { // TODO Response type
        adminService.updateCurrentChallenge()
        return ResponseEntity.ok("Yay, I did something")
    }

    @GetMapping("/challenge")
    final fun getCurrentChallenge(): ResponseEntity<String> {  // TODO Response type
        return ResponseEntity.ok("NOT IMPLEMENTED")
    }

}