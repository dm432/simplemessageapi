package com.danielmaile.simplemessageapi.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class CustomBadRequestException(
    override val message: String
) : Exception(message)

@ResponseStatus(HttpStatus.NOT_FOUND)
class UsernameNotFoundException(
    val username: String
) : Exception("The user $username was not found.")

@ResponseStatus(HttpStatus.CONFLICT)
class UsernameAlreadyTakenException : Exception("A user with that username already exists.")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidCredentialsException : Exception("Invalid username or password.")
