package xyz.engsmyre.moviedescriptiongame.common

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebInputException
import xyz.engsmyre.moviedescriptiongame.api.model.Problem

private val PROBLEM_JSON = MediaType.parseMediaType("application/problem+json")

@RestControllerAdvice
class ProblemExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ApiException::class)
    fun handleApi(ex: ApiException): ResponseEntity<Problem> =
        problem(ex.status, ex.code, ex.title, ex.detail)

    @ExceptionHandler(ServerWebInputException::class)
    fun handleInput(ex: ServerWebInputException): ResponseEntity<Problem> =
        problem(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_FAILED", "Invalid request", ex.reason)

    @ExceptionHandler(ResponseStatusException::class)
    fun handleStatus(ex: ResponseStatusException): ResponseEntity<Problem> =
        problem(ex.statusCode, ex.statusCode.toString().replace(' ', '_').uppercase(), "Request failed", ex.reason)

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<Problem> {
        log.error("Unhandled exception", ex)
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error", null)
    }

    private fun problem(status: HttpStatusCode, code: String, title: String, detail: String?): ResponseEntity<Problem> =
        ResponseEntity.status(status)
            .contentType(PROBLEM_JSON)
            .body(Problem(title = title, status = status.value(), code = code, detail = detail))
}
