package com.danielmaile.simplemessageapi.repository

import com.danielmaile.simplemessageapi.model.Message
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface MessageRepository : CoroutineCrudRepository<Message, Long> {
    suspend fun findAllByRecipientIdOrderByCreatedAsc(pageable: Pageable, recipientId: Long): Flow<Message>

    suspend fun countAllByRecipientId(recipientId: Long): Long
}
