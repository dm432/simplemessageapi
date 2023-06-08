package com.danielmaile.simplemessageapi.security

import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class JWTPropertiesTest {

    @Test
    fun `validityDuration has a default value and secretKey not`() {
        val properties = JWTProperties()
        expect {
            that(properties.validityDuration) isEqualTo 900000
            expectThrows<UninitializedPropertyAccessException> { properties.secretKey }
        }
    }
}

