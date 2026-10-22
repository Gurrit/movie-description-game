package xyz.engsmyre.moviedescriptiongame.common

import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import tools.jackson.databind.json.JsonMapper
import xyz.engsmyre.moviedescriptiongame.api.model.Problem

/** Writes problem+json from code outside the controller advice, such as web filters. */
@Component
class ProblemResponses(private val json: JsonMapper) {
    fun write(exchange: ServerWebExchange, status: Int, code: String, title: String, detail: String? = null): Mono<Void> {
        val response = exchange.response
        response.setRawStatusCode(status)
        response.headers.contentType = MediaType.parseMediaType("application/problem+json")
        val bytes = json.writeValueAsBytes(Problem(title = title, status = status, code = code, detail = detail))
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)))
    }
}
