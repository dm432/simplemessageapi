package com.danielmaile.simplemessageapi.web

import com.danielmaile.simplemessageapi.config.ApplicationConfig
import com.danielmaile.simplemessageapi.config.SecurityConfig
import com.danielmaile.simplemessageapi.model.CustomExceptionModel
import com.danielmaile.simplemessageapi.model.Message
import com.danielmaile.simplemessageapi.model.Role
import com.danielmaile.simplemessageapi.model.User
import com.danielmaile.simplemessageapi.repository.MessageRepository
import com.danielmaile.simplemessageapi.repository.UserRepository
import com.danielmaile.simplemessageapi.security.JWTProperties
import com.danielmaile.simplemessageapi.security.JWTTokenProvider
import com.danielmaile.simplemessageapi.web.model.NewMessage
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.time.LocalDateTime

typealias MessageDTO = com.danielmaile.simplemessageapi.web.model.Message

@WebFluxTest(
    controllers = [
        MessageController::class
    ]
)
@Import(
    ApplicationConfig::class,
    SecurityConfig::class,
    JWTTokenProvider::class,
    JWTProperties::class
)
@TestPropertySource(
    locations = [
        "classpath:properties.yaml"
    ]

)
class MessageControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var tokenProvider: JWTTokenProvider

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @MockkBean
    private lateinit var userRepository: UserRepository

    @MockkBean
    private lateinit var messageRepo: MessageRepository

    @Autowired
    private lateinit var authenticationManager: ReactiveAuthenticationManager

    private val createdTime = LocalDateTime.now()
    private lateinit var user1Token: String

    @BeforeEach
    fun setUp() {
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns createdTime

        every { messageRepo.save(any<Message>()) } answers {
            println(firstArg<Message>().id)
            println(firstArg<Message>().created)
            println(firstArg<Message>().senderId)
            println(firstArg<Message>().recipientId)
            println(firstArg<Message>().message)
            Mono.just(firstArg())
        }

        every { userRepository.findUserByUsername("user1") } answers {
            Mono.just(
                User(
                    id = 1,
                    username = "user1",
                    password = passwordEncoder.encode("password"),
                    roles = listOf(Role.ROLE_USER)
                )
            )
        }

        every { userRepository.findUserByUsername("user2") } answers {
            Mono.just(
                User(
                    id = 2,
                    username = "user2",
                    password = passwordEncoder.encode("password"),
                    roles = listOf(Role.ROLE_USER)
                )
            )
        }

        every { userRepository.findUserByUsername("notFound") } answers {
            Mono.empty()
        }

        user1Token = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                "user1",
                "password"
            )
        )
            .map(tokenProvider::createToken)
            .block()
            .orEmpty()
    }

    @Test
    fun `createMessage - creates a new message, saves and returns it`() {
        val response = webTestClient
            .post()
            .uri("/api/v1/message")
            .headers {
                it.setBearerAuth(user1Token)
            }
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(
                    NewMessage(
                        "user2",
                        "this is a test message."
                    )
                ),
                NewMessage::class.java
            )
            .exchange()

        verify(exactly = 1) {
            messageRepo.save(
                match {
                    it.created == createdTime &&
                            it.senderId == 1L &&
                            it.recipientId == 2L &&
                            it.message == "this is a test message."
                }
            )
        }

        response
            .expectStatus()
            .isCreated
            .expectBody(MessageDTO::class.java)
            .consumeWith {
                expectThat(it.responseBody)
                    .isNotNull()
                    .and {
                        get { created } isEqualTo createdTime
                        get { sender } isEqualTo "user1"
                        get { recipient } isEqualTo "user2"
                        get { message } isEqualTo "this is a test message."
                    }
            }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "",
            "   ",
            "    \r \n   \n",
            "\n \n \t    \r \t    "
        ]
    )
    fun `createMessage - returns HTTP 400 if message if empty or blank`(message: String) {
        val response = webTestClient
            .post()
            .uri("/api/v1/message")
            .headers {
                it.setBearerAuth(user1Token)
            }
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(
                    NewMessage(
                        "user2",
                        message
                    )
                ),
                NewMessage::class.java
            )
            .exchange()


        response
            .expectStatus()
            .isBadRequest
            .expectBody(CustomExceptionModel::class.java)
            .consumeWith {
                expectThat(it.responseBody)
                    .isNotNull()
            }
    }

    @Test
    fun `createMessage - returns HTTP 400 if recipient does not exist`() {
        val response = webTestClient
            .post()
            .uri("/api/v1/message")
            .headers {
                it.setBearerAuth(user1Token)
            }
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(
                    NewMessage(
                        "notFound",
                        "test message"
                    )
                ),
                NewMessage::class.java
            )
            .exchange()


        response
            .expectStatus()
            .isBadRequest
            .expectBody(CustomExceptionModel::class.java)
            .consumeWith {
                expectThat(it.responseBody)
                    .isNotNull()
            }
    }

    @Test
    fun `createMessage - returns HTTP 400 if sender and recipient are equal`() {
        val response = webTestClient
            .post()
            .uri("/api/v1/message")
            .headers {
                it.setBearerAuth(user1Token)
            }
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(
                    NewMessage(
                        "user1",
                        "test message"
                    )
                ),
                NewMessage::class.java
            )
            .exchange()


        response
            .expectStatus()
            .isBadRequest
            .expectBody(CustomExceptionModel::class.java)
            .consumeWith {
                expectThat(it.responseBody)
                    .isNotNull()
            }
    }
}