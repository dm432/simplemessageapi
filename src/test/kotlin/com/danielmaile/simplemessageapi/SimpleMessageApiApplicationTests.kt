package com.danielmaile.simplemessageapi

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(
    locations = [
        "classpath:properties.yaml"
    ]
)
class SimpleMessageApiApplicationTests {

    @Test
    fun contextLoads() {
    }
}
