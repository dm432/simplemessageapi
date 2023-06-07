package com.danielmaile.simplemessageapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Configuration
class ApplicationConfig {

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}