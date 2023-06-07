package com.danielmaile.simplemessageapi.config

import com.danielmaile.simplemessageapi.repository.UserRepository
import com.danielmaile.simplemessageapi.security.JWTTokenAuthenticationFilter
import com.danielmaile.simplemessageapi.security.JWTTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        tokenProvider: JWTTokenProvider,
        reactiveAuthenticationManager: ReactiveAuthenticationManager
    ): SecurityWebFilterChain = http
        .csrf { it.disable() }
        .httpBasic { it.disable() }
        .authenticationManager(reactiveAuthenticationManager)
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        .authorizeExchange {
            it.pathMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
            it.pathMatchers("/api-docs/**").permitAll()
            it.pathMatchers("/api-docs.html").permitAll()
            it.pathMatchers("/webjars/**").permitAll()
            it.anyExchange().authenticated()
        }
        .addFilterAt(JWTTokenAuthenticationFilter(tokenProvider), SecurityWebFiltersOrder.HTTP_BASIC)
        .build()

    @Bean
    fun userDetailsService(users: UserRepository) =
        ReactiveUserDetailsService { username: String ->
            users.findUserByUsername(username)
                .map { user ->
                    User
                        .withUsername(user.username)
                        .password(user.password)
                        .authorities(user.roles)
                        .accountExpired(!user.active)
                        .credentialsExpired(!user.active)
                        .disabled(!user.active)
                        .accountLocked(!user.active)
                        .build()
                }
        }

    @Bean
    fun reactiveAuthenticationManager(
        userDetailsService: ReactiveUserDetailsService,
        passwordEncoder: PasswordEncoder
    ): ReactiveAuthenticationManager {
        val authenticationManager = UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService)
        authenticationManager.setPasswordEncoder(passwordEncoder)
        return authenticationManager
    }
}