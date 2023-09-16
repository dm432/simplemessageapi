package com.danielmaile.simplemessageapi.web

import com.danielmaile.simplemessageapi.exception.CustomBadRequestException
import com.danielmaile.simplemessageapi.exception.UsernameNotFoundException
import com.danielmaile.simplemessageapi.model.Message
import com.danielmaile.simplemessageapi.repository.MessageRepository
import com.danielmaile.simplemessageapi.repository.UserRepository
import com.danielmaile.simplemessageapi.web.model.NewMessage
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

typealias MessageDTO = com.danielmaile.simplemessageapi.web.model.Message

@RestController
class MessageController : MessageAPI {

    @Autowired
    private lateinit var userRepo: UserRepository

    @Autowired
    private lateinit var messageRepo: MessageRepository

    override suspend fun createMessage(
        @AuthenticationPrincipal principal: UserDetails,
        @RequestBody message: NewMessage
    ): MessageDTO {
        if (message.message.isBlank()) {
            throw CustomBadRequestException("Message can not be empty.")
        }

        val senderId = userRepo
            .findUserByUsername(principal.username)
            ?.id
            ?: throw CustomBadRequestException("The current user was not found. Please try to log in again.")

        val recipient = userRepo
            .findUserByUsername(message.recipient)
            ?: throw UsernameNotFoundException(message.recipient)
        val recipientId = recipient
            .id
            ?: throw UsernameNotFoundException(message.recipient)

        if (senderId == recipient.id) {
            throw CustomBadRequestException("Sender and recipient can't be equal.")
        }

        val msg = messageRepo.save(
            Message(
                created = LocalDateTime.now(),
                senderId = senderId,
                recipientId = recipientId,
                message = message.message
            )
        )

        return MessageDTO(
            created = msg.created,
            sender = principal.username,
            recipient = recipient.username,
            message = message.message
        )
    }

    override suspend fun getMessages(
        @AuthenticationPrincipal principal: UserDetails,
        @RequestParam(defaultValue = "0", required = false, value = "page") page: Int,
        @RequestParam(defaultValue = "10", required = false, value = "size") size: Int
    ): Page<MessageDTO> {
        if (page < 0) {
            throw CustomBadRequestException("Page index must not be less than zero.")
        }
        if (size < 1) {
            throw CustomBadRequestException("Page size must not be less than one.")
        }
        val pageRequest = PageRequest.of(page, size)

        val currentUserId = userRepo
            .findUserByUsername(principal.username)
            ?.id
            ?: throw CustomBadRequestException("The current user was not found. Please try to log in again.")

        val messages = messageRepo
            .findAllByRecipientIdOrderByCreatedAsc(pageRequest, currentUserId)
            .toList()
            .map { message ->
                val sender = userRepo
                    .findById(message.senderId)

                MessageDTO(
                    created = message.created,
                    sender = sender?.username ?: "",
                    recipient = principal.username,
                    message = message.message
                )
            }

        return PageImpl(messages, pageRequest, messageRepo.countAllByRecipientId(currentUserId))
    }
}
