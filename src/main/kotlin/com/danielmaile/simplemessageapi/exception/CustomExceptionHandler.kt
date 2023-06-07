package com.danielmaile.simplemessageapi.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import reactor.core.publisher.Mono


@RestControllerAdvice
class CustomExceptionHandler {

    @ExceptionHandler(CustomException::class)
    fun handleUsernameAlreadyTaken(exception: CustomException): Mono<ResponseEntity<Map<String, String>>> {
        return Mono.just(
            ResponseEntity(
                mapOf(
                    "message" to exception.message
                ),
                exception.statusCode
            )
        )
    }
}