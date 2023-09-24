package com.danielmaile.simplemessageapi.repository

import com.danielmaile.simplemessageapi.model.Message
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface MessageRepository : CoroutineCrudRepository<Message, Long> {
    suspend fun findAllByRecipientIdOrderByCreatedAsc(pageable: Pageable, recipientId: Long): Flow<Message>

    suspend fun findAllByRecipientIdAndReadOrderByCreatedAsc(pageable: Pageable, recipientId: Long, read: Boolean): Flow<Message>

    suspend fun countAllByRecipientId(recipientId: Long): Long

    suspend fun countAllByRecipientIdAndRead(recipientId: Long, read: Boolean): Long

    @Modifying
    @Query(
        """
        update message
        set read = true
        where recipient = :recipientId 
    """
    )
    suspend fun readAllMessages(recipientId: Long)
}
