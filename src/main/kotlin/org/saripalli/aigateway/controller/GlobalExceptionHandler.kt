package org.saripalli.aigateway.controller

import org.saripalli.aigateway.service.LimitExceededException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    data class ErrorResponse(
        val error: ErrorDetail
    )

    data class ErrorDetail(
        val message: String,
        val type: String,
        val code: String? = null
    )

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        log.warn("Bad request: {}", ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(ErrorDetail(
                message = ex.message ?: "Bad request",
                type = "invalid_request_error"
            ))
        )
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        log.warn("Not found: {}", ex.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(ErrorDetail(
                message = ex.message ?: "Resource not found",
                type = "not_found_error"
            ))
        )
    }

    @ExceptionHandler(LimitExceededException::class)
    fun handleLimitExceeded(ex: LimitExceededException): ResponseEntity<ErrorResponse> {
        log.warn("Limit exceeded: {}", ex.message)
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
            ErrorResponse(ErrorDetail(
                message = ex.message ?: "Rate limit exceeded",
                type = "rate_limit_error"
            ))
        )
    }

    @ExceptionHandler(SecurityException::class)
    fun handleUnauthorized(ex: SecurityException): ResponseEntity<ErrorResponse> {
        log.warn("Auth error: {}", ex.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse(ErrorDetail(
                message = ex.message ?: "Unauthorized",
                type = "authentication_error"
            ))
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Internal error", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(ErrorDetail(
                message = "An internal error occurred",
                type = "server_error"
            ))
        )
    }
}