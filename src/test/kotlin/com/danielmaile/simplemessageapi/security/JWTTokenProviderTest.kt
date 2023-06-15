package com.danielmaile.simplemessageapi.security

import com.danielmaile.simplemessageapi.config.ApplicationConfig
import com.danielmaile.simplemessageapi.config.SecurityConfig
import com.danielmaile.simplemessageapi.model.Role
import com.danielmaile.simplemessageapi.repository.MessageRepository
import com.danielmaile.simplemessageapi.repository.UserRepository
import com.ninjasquad.springmockk.MockkBean
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.map
import java.nio.charset.StandardCharsets
import java.util.Base64

@WebFluxTest
@Import(
    JWTTokenProvider::class,
    SecurityConfig::class,
    ApplicationConfig::class
)
class JWTTokenProviderTest {

    @TestConfiguration
    class JWTTokenProviderTestConfig {
        @Bean
        fun jwtProperties(): JWTProperties = mockk {
            every { secretKey } returns "thisIsAVerySecureSecretKey"
            every { validityDuration } returns 900000
        }
    }

    @Autowired
    private lateinit var jwtProperties: JWTProperties

    @Autowired
    private lateinit var tokenProvider: JWTTokenProvider

    @MockkBean
    private lateinit var userRepository: UserRepository

    @MockkBean
    private lateinit var messageRepo: MessageRepository

    @Test
    fun `getAuthentication - extracts subject claim correctly`() {
        val claims = Jwts.claims(
            mapOf(
                Claims.SUBJECT to "TestUser"
            )
        )
        val token = getTokenForKey(jwtProperties.secretKey, claims)
        val authentication = tokenProvider.getAuthentication(token)

        expectThat(authentication.name) isEqualTo "TestUser"
    }

    @Test
    fun `getAuthentication - extracts multiple roles correctly`() {
        val claims = Jwts.claims(
            mapOf(
                Claims.SUBJECT to "TestUser",
                AUTHORITIES_KEY to "${Role.ROLE_USER.name},${Role.ROLE_ADMIN.name}"
            )
        )
        val token = getTokenForKey(jwtProperties.secretKey, claims)
        val authentication = tokenProvider.getAuthentication(token)

        expectThat(authentication.authorities)
            .isNotNull().and {
                hasSize(2)
            }
            .map { it.authority }
            .and {
                any { isEqualTo(Role.ROLE_USER.authority) }
                any { isEqualTo(Role.ROLE_ADMIN.authority) }
            }
    }

    @Test
    fun `getAuthentication - extracts one role correctly`() {
        val claims = Jwts.claims(
            mapOf(
                "sub" to "TestUser",
                AUTHORITIES_KEY to Role.ROLE_USER
            )
        )
        val token = getTokenForKey(jwtProperties.secretKey, claims)
        val authentication = tokenProvider.getAuthentication(token)

        expectThat(authentication.authorities)
            .isNotNull().and {
                hasSize(1)
            }
            .map { it.authority }
            .and {
                any { isEqualTo(Role.ROLE_USER.authority) }
            }
    }

    @Test
    fun `getAuthentication - extracts no role if claim is missing`() {
        val claims = Jwts.claims(
            mapOf(
                "sub" to "TestUser"
            )
        )
        val token = getTokenForKey(jwtProperties.secretKey, claims)
        val authentication = tokenProvider.getAuthentication(token)
        expectThat(authentication.authorities)
            .isNotNull()
            .and {
                hasSize(0)
            }
    }

    @Test
    fun `getAuthentication - throws exception for valid token with no subject claim`() {
        val token = getTokenForKey(jwtProperties.secretKey)
        expectThrows<IllegalArgumentException> { tokenProvider.getAuthentication(token) }
    }

    @Test
    fun `getAuthentication - throws exception for blank token`() {
        expectThrows<IllegalArgumentException> { tokenProvider.getAuthentication("") }
    }

    @Test
    fun `getAuthentication - throws exception for malformed token`() {
        expectThrows<MalformedJwtException> { tokenProvider.getAuthentication("verymalformedtoken") }
    }

    @Test
    fun `getAuthentication - throws exception for token with invalid secret key`() {
        val token = getTokenForKey("ThisIsAnInvalidSecretKey")
        expectThrows<SignatureException> { tokenProvider.getAuthentication(token) }
    }

    @Test
    fun `validateToken - returns true for valid token`() {
        val token = getTokenForKey(jwtProperties.secretKey)
        expectThat(tokenProvider.validateToken(token)) isEqualTo true
    }

    @Test
    fun `validateToken - returns false for token with invalid secret key`() {
        val token = getTokenForKey("ThisIsAnInvalidSecretKey")
        expectThat(tokenProvider.validateToken(token)) isEqualTo false
    }

    @Test
    fun `validateToken - returns false for blank token`() {
        expectThat(tokenProvider.validateToken("")) isEqualTo false
    }

    @Test
    fun `validateToken - returns false for invalid token format`() {
        expectThat(tokenProvider.validateToken("invalidtoken")) isEqualTo false
    }

    private fun getTokenForKey(
        key: String,
        claims: Claims = Jwts.claims(
            mapOf(
                "claim" to "thisisaclaim"
            )
        )
    ): String {
        val secret = Base64.getEncoder().encodeToString(key.toByteArray())
        val secretKey = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

        return Jwts
            .builder()
            .setClaims(claims)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }
}