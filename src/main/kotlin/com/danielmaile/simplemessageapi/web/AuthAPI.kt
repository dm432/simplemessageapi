package com.danielmaile.simplemessageapi.web

import com.danielmaile.simplemessageapi.model.User
import com.danielmaile.simplemessageapi.web.model.AuthRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
interface AuthAPI {

    @Operation(
        summary = "Login to an existing account.",
        description = "Returns a bearer token to authenticate on the other endpoints.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved bearer token.",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value = "{\n \"access_token\": \"XXXXXX\"\n}"
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid username or password.",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value = ""
                            )
                        ]
                    )
                ]
            )
        ]
    )
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    suspend fun login(@RequestBody authRequest: AuthRequest): Map<String, String>

    @Operation(
        summary = "Create a new user account.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Successfully created new account."
            ),
            ApiResponse(
                responseCode = "409",
                description = "User with that username already exists.",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value = ""
                            )
                        ]
                    )
                ]
            )
        ]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createAccount(@RequestBody authRequest: AuthRequest): User
}
