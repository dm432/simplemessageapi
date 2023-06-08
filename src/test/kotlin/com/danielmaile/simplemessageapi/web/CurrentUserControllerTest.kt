package com.danielmaile.simplemessageapi.web

import com.danielmaile.simplemessageapi.config.ApplicationConfig
import com.danielmaile.simplemessageapi.config.SecurityConfig
import com.danielmaile.simplemessageapi.model.Role
import com.danielmaile.simplemessageapi.model.User
import com.danielmaile.simplemessageapi.repository.UserRepository
import com.danielmaile.simplemessageapi.security.JWTProperties
import com.danielmaile.simplemessageapi.security.JWTTokenProvider
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@WebFluxTest(
    controllers = [
        CurrentUserController::class
    ]
)
@Import(
    ApplicationConfig::class,
    SecurityConfig::class,
    JWTTokenProvider::class,
    JWTProperties::class
)
class CurrentUserControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var tokenProvider: JWTTokenProvider

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @MockkBean
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var authenticationManager: ReactiveAuthenticationManager

    @Test
    fun `currentUser - returns the currently logged-in user`() {
        every { userRepository.findUserByUsername(any()) } answers {
            Mono.just(
                User(
                    username = "TestUser",
                    password = passwordEncoder.encode("password"),
                    roles = listOf(Role.ROLE_USER)
                )
            )
        }
        val token = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                "TestUser",
                "password"
            )
        )
            .map(tokenProvider::createToken)
            .block()
            .orEmpty()

        val response = webTestClient
            .get()
            .uri("/api/v1/me")
            .headers {
                it.setBearerAuth(token)
            }
            .exchange()


        response
            .expectStatus()
            .isOk
            .expectBody(User::class.java)
            .consumeWith {
                expectThat(it.responseBody)
                    .isNotNull()
                    .and {
                        get { username } isEqualTo "TestUser"
                        get { roles }.and {
                            hasSize(1)
                            any { isEqualTo(Role.ROLE_USER) }
                        }
                    }
            }
    }
}