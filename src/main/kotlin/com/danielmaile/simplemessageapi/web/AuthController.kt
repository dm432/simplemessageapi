package com.danielmaile.simplemessageapi.web

import com.danielmaile.simplemessageapi.exception.InvalidCredentialsException
import com.danielmaile.simplemessageapi.exception.UsernameAlreadyTakenException
import com.danielmaile.simplemessageapi.model.CustomExceptionModel
import com.danielmaile.simplemessageapi.model.Role
import com.danielmaile.simplemessageapi.model.User
import com.danielmaile.simplemessageapi.repository.UserRepository
import com.danielmaile.simplemessageapi.security.JWTTokenProvider
import com.danielmaile.simplemessageapi.web.model.AuthRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
class AuthController {

    @Autowired
    private lateinit var tokenProvider: JWTTokenProvider

    @Autowired
    private lateinit var authenticationManager: ReactiveAuthenticationManager

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var userRepo: UserRepository

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
                        schema = Schema(implementation = CustomExceptionModel::class)
                    )
                ]
            )
        ]
    )
    @PostMapping("/login")
    fun login(@RequestBody authRequest: Mono<AuthRequest>) =
        authRequest
            .flatMap { request ->
                authenticationManager
                    .authenticate(
                        UsernamePasswordAuthenticationToken(
                            request.username,
                            request.password
                        )
                    )
                    .map(tokenProvider::createToken)
            }
            .map { jwt ->
                val httpHeaders = HttpHeaders()
                httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer $jwt")
                val tokenBody = mapOf("access_token" to jwt)
                ResponseEntity(tokenBody, httpHeaders, HttpStatus.OK)
            }
            .onErrorResume {
                if (it is BadCredentialsException) {
                    throw InvalidCredentialsException()
                }

                throw it
            }

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
                        schema = Schema(implementation = CustomExceptionModel::class)
                    )
                ]
            )
        ]
    )
    @PostMapping
    fun createAccount(@RequestBody authRequest: Mono<AuthRequest>) =
        authRequest
            .flatMap { request ->
                val user = User(
                    username = request.username,
                    password = passwordEncoder.encode(request.password),
                    roles = listOf(Role.ROLE_USER)
                )
                userRepo.save(user)
            }
            .map {
                ResponseEntity(
                    it,
                    HttpStatus.CREATED
                )
            }
            .onErrorResume {
                if (it is DuplicateKeyException) {
                    throw UsernameAlreadyTakenException()
                }

                throw it
            }
}