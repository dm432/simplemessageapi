package com.danielmaile.simplemessageapi.exception

import org.springframework.http.HttpStatus

open class CustomException(
    override val message: String,
    val statusCode: HttpStatus
) : Exception()

class UsernameAlreadyTakenException : CustomException("A user with that username already exists.", HttpStatus.CONFLICT)

class InvalidCredentialsException : CustomException("Invalid username or password.", HttpStatus.BAD_REQUEST)

class UsernameNotFoundException(
    val username: String
) : CustomException("The user $username was not found.", HttpStatus.BAD_REQUEST)