package com.danielmaile.simplemessageapi.web

import com.danielmaile.simplemessageapi.config.ApplicationConfig
import com.danielmaile.simplemessageapi.config.SecurityConfig
import com.danielmaile.simplemessageapi.model.Role
import com.danielmaile.simplemessageapi.model.User
import com.danielmaile.simplemessageapi.repository.UserRepository
import com.danielmaile.simplemessageapi.security.JWTTokenProvider
import com.danielmaile.simplemessageapi.web.model.AuthRequest
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@WebFluxTest(
    controllers = [
        AuthController::class
    ]
)
@Import(
    ApplicationConfig::class,
    SecurityConfig::class
)
@TestPropertySource(
    locations = [
        "classpath:properties.yaml"
    ]

)
class AuthControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var tokenProvider: JWTTokenProvider

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @MockkBean
    private lateinit var userRepository: UserRepository

    @Test
    fun `createAccount - creates new account and returns user entity`() {
        coEvery { userRepository.save(any()) } answers {
            firstArg()
        }
        coEvery { userRepository.findUserByUsername(any()) } answers {
            null
        }

        val response = webTestClient
            .post()
            .uri("/api/v1/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(
                    AuthRequest(
                        username = "TestUser",
                        password = "password"
                    )
                ),
                AuthRequest::class.java
            )
            .exchange()

        response
            .expectStatus()
            .isCreated
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

    @Test
    fun `createAccount - returns HTTP 409 if user with that name already exists`() {
        coEvery { userRepository.findUserByUsername(any()) } answers {
            User(
                username = "TestUser"
            )
        }

        val response = webTestClient
            .post()
            .uri("/api/v1/auth")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(
                    AuthRequest(
                        username = "TestUser",
                        password = "password"
                    )
                ),
                AuthRequest::class.java
            )
            .exchange()

        response
            .expectStatus()
            .isEqualTo(409)
    }

    @Test
    fun `login - returns bearer token if credentials are valid`() {
        coEvery { userRepository.findUserByUsername(any()) } answers {
            User(
                username = firstArg(),
                password = passwordEncoder.encode("password")
            )
        }
        every { tokenProvider.createToken(any()) } returns "abearertoken"

        val response = webTestClient
            .post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(
                    AuthRequest(
                        username = "TestUser",
                        password = "password"
                    )
                ),
                AuthRequest::class.java
            )
            .exchange()

        response
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("\$.access_token").isEqualTo("abearertoken")
    }

    @Test
    fun `login - returns HTTP 400 if username is invalid`() {
        coEvery { userRepository.findUserByUsername(any()) } answers {
            null
        }

        val response = webTestClient
            .post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(
                    AuthRequest(
                        username = "TestUser",
                        password = "password"
                    )
                ),
                AuthRequest::class.java
            )
            .exchange()

        response
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `login - returns HTTP 400 if password is invalid`() {
        coEvery { userRepository.findUserByUsername(any()) } answers {
            User(
                username = "TestUser",
                password = passwordEncoder.encode("invalidPassword")
            )
        }

        val response = webTestClient
            .post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(
                    AuthRequest(
                        username = "TestUser",
                        password = "password"
                    )
                ),
                AuthRequest::class.java
            )
            .exchange()

        response
            .expectStatus()
            .isBadRequest
    }
}
