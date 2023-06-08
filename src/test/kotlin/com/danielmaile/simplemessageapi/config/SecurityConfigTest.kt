package com.danielmaile.simplemessageapi.config

import com.danielmaile.simplemessageapi.model.Role
import com.danielmaile.simplemessageapi.model.User
import com.danielmaile.simplemessageapi.repository.UserRepository
import com.danielmaile.simplemessageapi.security.JWTProperties
import com.danielmaile.simplemessageapi.security.JWTTokenProvider
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isContainedIn
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNotNull
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.Date
import java.util.stream.Collectors

@Import(
    SecurityConfig::class,
    ApplicationConfig::class,
    JWTTokenProvider::class,
    JWTProperties::class
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityConfigTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var userDetailsService: ReactiveUserDetailsService

    @Autowired
    private lateinit var authenticationManager: ReactiveAuthenticationManager

    @MockkBean
    private lateinit var userRepository: UserRepository

    @SpykBean
    private lateinit var tokenProvider: JWTTokenProvider

    @Autowired
    private lateinit var jwtProperties: JWTProperties

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/api/v1/auth",
            "/api/v1/auth/login"
        ]
    )
    fun `springSecurityFilterChain - auth endpoints are accessible without authentication`(endpoint: String) {
        val response = webTestClient
            .post()
            .uri(endpoint)
            .exchange()

        response
            .expectBody()
            .consumeWith {
                expectThat(it.status) isNotEqualTo HttpStatus.UNAUTHORIZED
                expectThat(it.status) isNotEqualTo HttpStatus.FORBIDDEN
            }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/api-docs",
            "/api-docs.html",
            "/webjars/swagger-ui/index.html"
        ]
    )
    fun `springSecurityFilterChain - api docs and swagger ui are accessible without authentication`(endpoint: String) {
        val response = webTestClient
            .head()
            .uri(endpoint)
            .exchange()

        response
            .expectBody()
            .consumeWith {
                expect {

                }
                expectThat(it.status).isContainedIn(
                    listOf(
                        HttpStatus.OK,
                        HttpStatus.FOUND
                    )
                )
            }
    }

    @ParameterizedTest
    @ValueSource(strings = ["/api/v1/me"])
    fun `springSecurityFilterChain - authenticated endpoints are accessible with valid bearer token`(endpoint: String) {
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
            .head()
            .uri(endpoint)
            .headers {
                it.setBearerAuth(token)
            }
            .exchange()

        response
            .expectStatus()
            .isOk
    }

    @ParameterizedTest
    @ValueSource(strings = ["/api/v1/me"])
    fun `springSecurityFilterChain - authenticated endpoints are not accessible without authentication`(endpoint: String) {
        val response = webTestClient
            .head()
            .uri(endpoint)
            .exchange()

        response
            .expectStatus()
            .isUnauthorized
    }

    @ParameterizedTest
    @ValueSource(strings = ["/api/v1/me"])
    fun `springSecurityFilterChain - authenticated endpoints are not accessible with empty bearer token`(endpoint: String) {
        val response = webTestClient
            .head()
            .uri(endpoint)
            .headers {
                it.setBearerAuth("")
            }
            .exchange()

        response
            .expectStatus()
            .isUnauthorized
    }

    @ParameterizedTest
    @ValueSource(strings = ["/api/v1/me"])
    fun `springSecurityFilterChain - authenticated endpoints are not accessible with invalid bearer token`(endpoint: String) {
        val response = webTestClient
            .head()
            .uri(endpoint)
            .headers {
                it.setBearerAuth("invalid token")
            }
            .exchange()

        response
            .expectStatus()
            .isUnauthorized
    }

    @ParameterizedTest
    @ValueSource(strings = ["/api/v1/me"])
    fun `springSecurityFilterChain - authenticated endpoints are not accessible with expired bearer token`(endpoint: String) {
        every { userRepository.findUserByUsername(any()) } answers {
            Mono.just(
                User(
                    username = "TestUser",
                    password = passwordEncoder.encode("password"),
                    roles = listOf(Role.ROLE_USER)
                )
            )
        }
        every { tokenProvider.createToken(any()) } answers {
            val username = firstArg<Authentication>().name
            val authorities = firstArg<Authentication>().authorities
            val claims = Jwts.claims().setSubject(username)
            val secret = Base64.getEncoder().encodeToString(jwtProperties.secretKey.toByteArray())
            val secretKey = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

            if (authorities.isEmpty()) {
                claims["roles"] = authorities.stream()
                    .map { it.authority }
                    .collect(Collectors.joining(","))
            }

            val now = Date()
            val validity = Date(now.time - 1)

            Jwts
                .builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact()
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
            .head()
            .uri(endpoint)
            .headers {
                it.setBearerAuth(token)
            }
            .exchange()

        response
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `userDetailsService - maps correctly`() {
        every { userRepository.findUserByUsername(any()) } answers {
            Mono.just(
                User(
                    id = 123,
                    username = firstArg(),
                    password = "password",
                    active = true,
                    roles = listOf(Role.ROLE_USER, Role.ROLE_ADMIN)
                )
            )
        }

        val userDetails = userDetailsService
            .findByUsername("TestUser")
            .block()

        expectThat(userDetails)
            .isNotNull()
            .and {
                get { username } isEqualTo "TestUser"
                get { password } isEqualTo "password"
                get { authorities }
                    .isNotNull()
                    .and {
                        hasSize(2)
                        any { isEqualTo(Role.ROLE_USER) }
                        any { isEqualTo(Role.ROLE_ADMIN) }
                    }
                get { isAccountNonExpired } isEqualTo true
                get { isCredentialsNonExpired } isEqualTo true
                get { isEnabled } isEqualTo true
                get { isAccountNonLocked } isEqualTo true
            }
    }
}