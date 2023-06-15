package com.danielmaile.simplemessageapi.repository

import com.danielmaile.simplemessageapi.model.User
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface UserRepository : ReactiveCrudRepository<User, Long> {

    fun findUserByUsername(username: String): Mono<User>
}