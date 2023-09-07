package com.danielmaile.simplemessageapi.web

import com.danielmaile.simplemessageapi.exception.InvalidCredentialsException
import com.danielmaile.simplemessageapi.exception.UsernameAlreadyTakenException
import com.danielmaile.simplemessageapi.model.Role
import com.danielmaile.simplemessageapi.model.User
import com.danielmaile.simplemessageapi.repository.UserRepository
import com.danielmaile.simplemessageapi.security.JWTTokenProvider
import com.danielmaile.simplemessageapi.web.model.AuthRequest
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController : AuthAPI {

    @Autowired
    private lateinit var tokenProvider: JWTTokenProvider

    @Autowired
    private lateinit var authenticationManager: ReactiveAuthenticationManager

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var userRepo: UserRepository

    override suspend fun login(authRequest: AuthRequest): Map<String, String> {
        try {
            val authentication = authenticationManager
                .authenticate(
                    UsernamePasswordAuthenticationToken(
                        authRequest.username,
                        authRequest.password
                    )
                )
                .awaitSingle()

            val token = tokenProvider.createToken(authentication)
            return mapOf("access_token" to token)
        } catch (e: BadCredentialsException) {
            throw InvalidCredentialsException()
        }
    }

    override suspend fun createAccount(authRequest: AuthRequest): User {
        if (userRepo.findUserByUsername(authRequest.username) != null) {
            throw UsernameAlreadyTakenException()
        }

        return userRepo.save(
            User(
                username = authRequest.username,
                password = passwordEncoder.encode(authRequest.password),
                roles = listOf(Role.ROLE_USER)
            )
        )
    }
}
