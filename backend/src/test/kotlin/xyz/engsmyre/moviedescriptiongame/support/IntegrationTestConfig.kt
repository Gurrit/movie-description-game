package xyz.engsmyre.moviedescriptiongame.support

import com.github.tomakehurst.wiremock.WireMockServer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.DynamicPropertyRegistrar
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/** Real Postgres, a fake TMDB and a fixed clock. Shared by all integration tests so the context is cached. */
@TestConfiguration(proxyBeanMethods = false)
class IntegrationTestConfig {
    companion object {
        val NOW: Instant = Instant.parse("2026-10-22T12:00:00Z")
    }

    @Bean
    @ServiceConnection
    fun postgres(): PostgreSQLContainer = PostgreSQLContainer("postgres:17-alpine")

    @Bean(destroyMethod = "stop")
    fun tmdbServer(): WireMockServer = TmdbWireMock.start()

    @Bean
    @Primary
    fun fixedClock(): Clock = Clock.fixed(NOW, ZoneOffset.UTC)

    @Bean
    fun tmdbProperties(server: WireMockServer) = DynamicPropertyRegistrar { registry ->
        registry.add("tmdb.base-url") { "http://localhost:${server.port()}/3" }
        registry.add("tmdb.read-token") { "test-token" }
        registry.add("tmdb.retry-backoff-millis") { "10" }
        registry.add("generation.internal-token") { INTERNAL_TOKEN }
        registry.add("generation.scheduler-enabled") { "false" }
        registry.add("generation.pre-generate-days") { "1" }
    }
}

const val INTERNAL_TOKEN = "test-internal-token"
