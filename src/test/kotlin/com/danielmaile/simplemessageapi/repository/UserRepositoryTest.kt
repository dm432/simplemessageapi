package com.danielmaile.simplemessageapi.repository

import com.danielmaile.simplemessageapi.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import reactor.test.StepVerifier

@DataR2dbcTest
class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        userRepository
            .deleteAll()
            .subscribe()

        userRepository
            .save(
                User(
                    username = "TestUser"
                )
            )
            .subscribe()

        userRepository
            .save(
                User(
                    username = "Bob"
                )
            )
            .subscribe()
    }

    @Test
    fun `findUserByUsername - returns empty mono for non existent user`() {
        val alice = userRepository.findUserByUsername("Alice")

        StepVerifier
            .create(alice)
            .expectComplete()
            .verify()
    }

    @Test
    fun `findUserByUsername - returns correct user for given username`() {
        val testUser = userRepository.findUserByUsername("TestUser")
        StepVerifier
            .create(testUser)
            .expectNextMatches {
                it.username == "TestUser"
            }
            .expectComplete()
            .verify()

        val bob = userRepository.findUserByUsername("Bob")
        StepVerifier
            .create(bob)
            .expectNextMatches {
                it.username == "Bob"
            }
            .expectComplete()
            .verify()
    }
}