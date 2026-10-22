package xyz.engsmyre.moviedescriptiongame

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer

/** Real Postgres for integration tests; the version matches production. */
@TestConfiguration(proxyBeanMethods = false)
class PostgresTestConfig {
    @Bean
    @ServiceConnection
    fun postgres(): PostgreSQLContainer = PostgreSQLContainer("postgres:17-alpine")
}
