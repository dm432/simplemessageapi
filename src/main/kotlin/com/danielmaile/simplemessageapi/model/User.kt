package com.danielmaile.simplemessageapi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("\"user\"")
class User (
    @Id
    @JsonIgnore
    var id: Long? = null,
    val username: String,
    @JsonIgnore
    val password: String = "",
    @JsonIgnore
    val active: Boolean = true,
    val roles: List<Role> = listOf()
)