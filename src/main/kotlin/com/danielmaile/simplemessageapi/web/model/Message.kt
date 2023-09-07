package com.danielmaile.simplemessageapi.web.model

import java.time.LocalDateTime

data class Message(
    val created: LocalDateTime,
    val sender: String,
    val recipient: String,
    val message: String
)
