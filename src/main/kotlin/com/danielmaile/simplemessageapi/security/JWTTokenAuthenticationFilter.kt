package com.danielmaile.simplemessageapi.security

import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

private const val HEADER_PREFIX = "Bearer "

class JWTTokenAuthenticationFilter(
    private val tokenProvider: JWTTokenProvider
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val token = resolveToken(exchange.request)

        if (!token.isNullOrBlank() && tokenProvider.validateToken(token)) {
            val authentication = tokenProvider.getAuthentication(token)
            return chain
                .filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
        }
        return chain.filter(exchange)
    }

    private fun resolveToken(request: ServerHttpRequest): String? {
        val bearerToken = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (!bearerToken.isNullOrBlank() && bearerToken.startsWith(HEADER_PREFIX)) {
            return bearerToken.substring(7)
        }

        return null
    }
}