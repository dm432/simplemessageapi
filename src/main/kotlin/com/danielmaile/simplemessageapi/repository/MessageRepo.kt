package com.danielmaile.simplemessageapi.repository

import com.danielmaile.simplemessageapi.model.Message
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface MessageRepository : ReactiveCrudRepository<Message, Long> {
   fun findAllByRecipientIdOrderByCreatedAsc(recipientId: Long): Flux<Message>
}