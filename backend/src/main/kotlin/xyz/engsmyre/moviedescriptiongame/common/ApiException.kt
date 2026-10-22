package xyz.engsmyre.moviedescriptiongame.common

import org.springframework.http.HttpStatus

/** An error that maps to an RFC 9457 problem response with a stable machine-readable [code]. */
open class ApiException(
    val status: HttpStatus,
    val code: String,
    val title: String,
    detail: String? = null,
) : RuntimeException(detail ?: title) {
    val detail: String? = detail
}
