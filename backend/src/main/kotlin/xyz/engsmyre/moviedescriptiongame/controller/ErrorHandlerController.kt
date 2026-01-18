package xyz.engsmyre.moviedescriptiongame.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.RuntimeException

@ControllerAdvice
final class ErrorHandlerController : ResponseEntityExceptionHandler() {
    @ExceptionHandler(value = [RuntimeException::class])
    final fun handleDefaultErrorPage(exception : ResponseStatusException) : ResponseEntity<ResponseStatusException> {
        return ResponseEntity(exception, exception.statusCode)
    }
}