package com.danielmaile.simplemessageapi.web.model

data class NewMessage(
    val recipient: String,
    val message: String
)