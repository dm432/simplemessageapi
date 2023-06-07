package com.danielmaile.simplemessageapi.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "Exception")
data class CustomExceptionModel(
    val message: String
)