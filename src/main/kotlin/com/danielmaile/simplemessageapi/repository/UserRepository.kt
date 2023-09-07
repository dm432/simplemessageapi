package com.danielmaile.simplemessageapi.repository

import com.danielmaile.simplemessageapi.model.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository : CoroutineCrudRepository<User, Long> {

    suspend fun findUserByUsername(username: String): User?
}
