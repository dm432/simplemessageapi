package com.danielmaile.simplemessageapi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("message")
class Message(
    @Id
    @JsonIgnore
    var id: Long? = null,
    val created: LocalDateTime,
    @Column("sender")
    val senderId: Long,
    @Column("recipient")
    val recipientId: Long,
    val read: Boolean,
    val message: String
)
