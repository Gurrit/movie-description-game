package xyz.engsmyre.moviedescriptiongame.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Movie Description Game API")
                    .version("1.0.0")
                    .description(
                        "A daily movie guessing game where players guess movies based on progressive clues. " +
                                "Each day, 5 random movies are selected. Players request clues (year, director, actors, etc.), " +
                                "lose points per clue, and submit guesses to earn scores."
                    )
                    .contact(
                        Contact()
                            .name("Movie Description Game")
                            .email("support@moviedescription.game")
                    )
                    .license(
                        License()
                            .name("MIT")
                    )
            )
            .servers(
                listOf(
                    Server().url("http://localhost:8080").description("Local development server"),
                    Server().url("https://api.moviedescription.game").description("Production server")
                )
            )
    }
}
