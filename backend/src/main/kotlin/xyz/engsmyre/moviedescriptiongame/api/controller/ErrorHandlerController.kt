package xyz.engsmyre.moviedescriptiongame.api.controller

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ResponseStatusException
import xyz.engsmyre.moviedescriptiongame.api.dto.ErrorResponse
import xyz.engsmyre.moviedescriptiongame.exceptions.CustomException
import java.time.format.DateTimeParseException

@Hidden  // Hide from Swagger UI as it's global error handling
@ControllerAdvice
class ErrorHandlerController {

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(ex: CustomException): ResponseEntity<ErrorResponse> {
        println("EXCEPTION $ex")
        return ResponseEntity.status(ex.statusCode).body(
            ErrorResponse(
                error = "${ex.statusCode.value()}",
                message = ex.reason ?: "An error occurred",
                details = mapOf("status" to ex.statusCode.value())
            )
        )
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(ex: ResponseStatusException): ResponseEntity<ErrorResponse> {
        println("EXCEPTION $ex")
        return ResponseEntity.status(ex.statusCode).body(
            ErrorResponse(
                error = "${ex.statusCode.value()}",
                message = ex.reason ?: "An error occurred",
                details = mapOf("status" to ex.statusCode.value())
            )
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        println("EXCEPTION $ex")
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                error = "BAD_REQUEST",
                message = ex.message ?: "Invalid request",
                details = mapOf("cause" to ex.javaClass.simpleName)
            )
        )
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        println("EXCEPTION $ex")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(
                error = "INTERNAL_ERROR",
                message = ex.message ?: "Internal server error",
                details = mapOf("cause" to ex.javaClass.simpleName)
            )
        )
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        println("EXCEPTION $ex")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(
                error = "NOT_FOUND",
                message = ex.message ?: "Resource not found",
                details = mapOf("cause" to ex.javaClass.simpleName)
            )
        )
    }

    @ExceptionHandler(DateTimeParseException::class)
    fun handleDateTimeParseException(ex: DateTimeParseException): ResponseEntity<ErrorResponse> {
        println("EXCEPTION $ex")
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                error = "INVALID_DATE_FORMAT",
                message = "Invalid date format. Expected format: yyyy-MM-dd",
                details = mapOf(
                    "parsedString" to ex.parsedString,
                    "errorIndex" to ex.errorIndex
                )
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        // Don't expose internal error details in production
        println("EXCEPTION $ex")
        return ResponseEntity.internalServerError().body(
            ErrorResponse(
                error = "INTERNAL_SERVER_ERROR",
                message = "An unexpected error occurred",
                details = null
            )
        )
    }
}
