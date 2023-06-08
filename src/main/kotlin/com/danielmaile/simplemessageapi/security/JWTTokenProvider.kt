package com.danielmaile.simplemessageapi.security

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.Date
import java.util.stream.Collectors
import javax.crypto.SecretKey

private const val AUTHORITIES_KEY = "roles"

@Component
class JWTTokenProvider {

    @Autowired
    private lateinit var jwtProperties: JWTProperties

    private lateinit var secretKey: SecretKey

    @PostConstruct
    fun init() {
        val secret = Base64.getEncoder().encodeToString(jwtProperties.secretKey.toByteArray())
        secretKey = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))
    }

    fun createToken(authentication: Authentication): String {
        val username = authentication.name
        val authorities = authentication.authorities
        val claims = Jwts.claims().setSubject(username)

        if (authorities.isEmpty()) {
            claims[AUTHORITIES_KEY] = authorities.stream()
                .map { it.authority }
                .collect(Collectors.joining(","))
        }

        val now = Date()
        val validity = Date(now.time + jwtProperties.validityDuration)

        return Jwts
            .builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val claims =
            Jwts
                .parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body

        val authoritiesClaim = claims[AUTHORITIES_KEY]
        val authorities =
            if (authoritiesClaim == null) {
                AuthorityUtils.NO_AUTHORITIES
            } else {
                AuthorityUtils
                    .commaSeparatedStringToAuthorityList(authoritiesClaim.toString())
            }
        val principal = User(claims.subject, "", authorities)

        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts
                .parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}