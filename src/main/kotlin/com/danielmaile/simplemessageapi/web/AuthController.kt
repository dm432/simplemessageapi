package com.danielmaile.simplemessageapi.web

import com.danielmaile.simplemessageapi.model.Role
import com.danielmaile.simplemessageapi.model.User
import com.danielmaile.simplemessageapi.repository.UserRepository
import com.danielmaile.simplemessageapi.security.JWTTokenProvider
import com.danielmaile.simplemessageapi.web.model.AuthRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
class AuthController {

    @Autowired
    private lateinit var tokenProvider: JWTTokenProvider

    @Autowired
    private lateinit var authenticationManager: ReactiveAuthenticationManager

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var userRepo: UserRepository

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
}