package xyz.engsmyre.moviedescriptiongame.support

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper

class ApiResult(val status: Int, val body: JsonNode) {
    val code: String? get() = body.get("code")?.asString()
    operator fun get(field: String): JsonNode = body.get(field) ?: error("no field '$field' in $body")
    fun has(field: String): Boolean = body.has(field)
}

/** Calls the API over HTTP and checks every exchange against the OpenAPI contract. */
class ApiClient(val web: WebTestClient, private val json: JsonMapper) {

    fun call(
        method: String,
        path: String,
        token: String? = null,
        body: String? = null,
        query: Map<String, String> = emptyMap(),
        validate: Boolean = true,
    ): ApiResult {
        // The test client already has the /api base path of the application
        val uri = path + if (query.isEmpty()) "" else query.entries.joinToString("&", "?") { "${it.key}=${it.value}" }
        val spec = web.method(org.springframework.http.HttpMethod.valueOf(method)).uri(uri).apply {
            if (token != null) header("Authorization", "Bearer $token")
        }
        val exchange = (if (body != null) spec.contentType(MediaType.APPLICATION_JSON).bodyValue(body) else spec).exchange()
        val result = exchange.expectBody(String::class.java).returnResult()
        val text = result.responseBody
        val status = result.status.value()
        if (validate) {
            val problems = ContractValidator.violations(
                method, "/api$path", query, body, token != null, status, text, result.responseHeaders.contentType?.toString(),
            )
            assertTrue(problems.isEmpty(), "Contract violations for $method $path ($status):\n$problems\n$text")
        }
        return ApiResult(status, if (text.isNullOrBlank()) json.createObjectNode() else json.readTree(text))
    }

    fun createPlayer(): String {
        val r = call("POST", "/players")
        assertEquals(201, r.status)
        return r["token"].asString()
    }

    fun start(token: String) = call("POST", "/game/today", token)
    fun today(token: String) = call("GET", "/game/today", token)
    fun reveal(token: String, clueId: String) = call("POST", "/game/today/rounds/current/clues/$clueId/reveal", token)
    fun guess(token: String, tmdbId: Int) = call("POST", "/game/today/rounds/current/guesses", token, """{"tmdbId":$tmdbId}""")
    fun stats(token: String) = call("GET", "/players/me/stats", token)
}
