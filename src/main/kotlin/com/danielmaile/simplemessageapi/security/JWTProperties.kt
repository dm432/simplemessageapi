package com.danielmaile.simplemessageapi.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class JWTProperties {

    @Value("\${jwt.secretKey}")
    lateinit var secretKey: String

    @Value("\${jwt.validityDuration}")
    var validityDuration: Long = 900000
}
