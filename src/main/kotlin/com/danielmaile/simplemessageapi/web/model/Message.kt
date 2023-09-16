package com.danielmaile.simplemessageapi.web.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class Message @JsonCreator constructor(
    @JsonProperty("created") val created: LocalDateTime,
    @JsonProperty("sender") val sender: String,
    @JsonProperty("recipient") val recipient: String,
    @JsonProperty("message") val message: String
)
