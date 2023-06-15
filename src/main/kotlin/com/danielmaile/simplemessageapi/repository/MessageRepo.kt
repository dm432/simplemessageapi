package com.danielmaile.simplemessageapi.repository

import com.danielmaile.simplemessageapi.model.Message
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface MessageRepository : ReactiveCrudRepository<Message, Long>