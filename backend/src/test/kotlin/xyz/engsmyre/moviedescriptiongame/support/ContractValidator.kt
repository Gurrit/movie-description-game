package xyz.engsmyre.moviedescriptiongame.support

import com.atlassian.oai.validator.OpenApiInteractionValidator
import com.atlassian.oai.validator.model.Request
import com.atlassian.oai.validator.model.SimpleRequest
import com.atlassian.oai.validator.model.SimpleResponse
import java.nio.file.Path

/** Checks real request/response pairs against api/openapi.yaml, so the server cannot drift from the contract. */
object ContractValidator {
    private val validator: OpenApiInteractionValidator = OpenApiInteractionValidator
        .createFor(Path.of("..", "api", "openapi.yaml").toAbsolutePath().normalize().toString())
        .build()

    /** Returns an empty string when the interaction conforms to the contract, else the problems found. */
    fun violations(
        method: String,
        path: String,
        query: Map<String, String>,
        requestBody: String?,
        authorized: Boolean,
        status: Int,
        responseBody: String?,
        contentType: String?,
    ): String {
        val request = SimpleRequest.Builder(method, path).apply {
            query.forEach { (k, v) -> withQueryParam(k, v) }
            if (authorized) withAuthorization("Bearer token") else Unit
            if (requestBody != null) withBody(requestBody).withContentType("application/json")
            withAccept("application/json")
        }.build()
        val response = SimpleResponse.Builder.status(status).apply {
            if (responseBody != null) withBody(responseBody)
            if (contentType != null) withContentType(contentType.substringBefore(';'))
        }.build()
        val report = validator.validate(request as Request, response)
        return report.messages.filter { it.level.name == "ERROR" }.joinToString("\n") { "${it.key}: ${it.message}" }
    }
}
