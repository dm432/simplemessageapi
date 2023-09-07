package com.danielmaile.simplemessageapi.repository

import com.danielmaile.simplemessageapi.model.Message
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface MessageRepository : CoroutineCrudRepository<Message, Long> {
    suspend fun findAllByRecipientIdOrderByCreatedAsc(recipientId: Long): Flow<Message>
}
