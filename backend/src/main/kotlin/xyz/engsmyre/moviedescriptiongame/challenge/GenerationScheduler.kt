package xyz.engsmyre.moviedescriptiongame.challenge

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import xyz.engsmyre.moviedescriptiongame.config.GenerationProperties
import java.time.Clock
import java.time.LocalDate

/** Creates the game for today (and optionally the coming days) at the configured time. */
@Component
@ConditionalOnProperty("generation.scheduler-enabled", matchIfMissing = true)
class GenerationScheduler(
    private val generator: GameGenerator,
    private val props: GenerationProperties,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Scheduled(cron = "\${generation.cron}", zone = "UTC")
    fun generate() {
        scope.launch {
            val today = LocalDate.now(clock)
            for (offset in 0..props.preGenerateDays) {
                val date = today.plusDays(offset.toLong())
                try {
                    generator.ensure(date)
                } catch (e: Exception) {
                    // Players hitting the lazy path will retry; the failure is already logged with details
                    log.error("Scheduled generation for {} failed", date, e)
                }
            }
        }
    }
}
