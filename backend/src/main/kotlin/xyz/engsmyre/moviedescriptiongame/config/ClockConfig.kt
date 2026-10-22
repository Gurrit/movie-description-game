package xyz.engsmyre.moviedescriptiongame.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class ClockConfig {
    /** All game logic uses UTC; "today" is `LocalDate.now(clock)`. */
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
