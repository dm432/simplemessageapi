package com.danielmaile.simplemessageapi.web

import com.danielmaile.simplemessageapi.exception.CustomException
import com.danielmaile.simplemessageapi.exception.UsernameNotFoundException
import com.danielmaile.simplemessageapi.model.CustomExceptionModel
import com.danielmaile.simplemessageapi.model.Message
import com.danielmaile.simplemessageapi.repository.MessageRepository
import com.danielmaile.simplemessageapi.repository.UserRepository
import com.danielmaile.simplemessageapi.web.model.NewMessage
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDateTime

typealias MessageDTO = com.danielmaile.simplemessageapi.web.model.Message

@RestController
@RequestMapping("api/v1")
@Tag(name = "Message")
class MessageController {

    @Autowired
    private lateinit var userRepo: UserRepository

    @Autowired
    private lateinit var messageRepo: MessageRepository

    @Operation(
        summary = "Create a new message.",
        description = "Creates a new message that's send to another user",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Successfully created new message.",
                content = [
                    Content(
                        schema = Schema(implementation = MessageDTO::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "The provided recipient does not exist, the provided recipient is equal to the current user or the message is blank.",
                content = [
                    Content(
                        schema = Schema(implementation = CustomExceptionModel::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value = ""
                            )
                        ]
                    )
                ]
            )
        ]
    )
    @PostMapping("/message")
    fun createMessage(
        @AuthenticationPrincipal principal: Mono<UserDetails>,
        @RequestBody message: Mono<NewMessage>
    ): Mono<ResponseEntity<MessageDTO>> =
        principal
            .flatMap { userDetails ->
                message.flatMap { newMessage ->

                    if (newMessage.message.isBlank()) {
                        return@flatMap Mono.error(
                            CustomException(
                                "Message can not be empty.",
                                HttpStatus.BAD_REQUEST
                            )
                        )
                    }

                    val senderId = userRepo
                        .findUserByUsername(userDetails.username)
                        .mapNotNull { it.id }
                        .switchIfEmpty {
                            Mono.error(
                                CustomException(
                                    "The current user was not found. Please try to log in again.",
                                    HttpStatus.INTERNAL_SERVER_ERROR
                                )
                            )
                        }

                    val recipientId = userRepo
                        .findUserByUsername(newMessage.recipient)
                        .mapNotNull { it.id }
                        .switchIfEmpty {
                            Mono.error(UsernameNotFoundException(newMessage.recipient))
                        }

                    senderId
                        .zipWith(recipientId)
                        .flatMap {
                            if (it.t1 == it.t2) {
                                Mono.error(
                                    CustomException(
                                        "Sender and recipient can't be equal.",
                                        HttpStatus.BAD_REQUEST
                                    )
                                )
                            } else {
                                messageRepo.save(
                                    Message(
                                        created = LocalDateTime.now(),
                                        senderId = it.t1,
                                        recipientId = it.t2,
                                        message = newMessage.message
                                    )
                                )
                            }
                        }
                        .map {
                            MessageDTO(
                                created = it.created,
                                sender = userDetails.username,
                                recipient = newMessage.recipient,
                                message = newMessage.message
                            )
                        }
                }
            }
            .map {
                ResponseEntity(
                    it,
                    HttpStatus.CREATED
                )
            }
            .onErrorResume {
                throw it
            }

    @Operation(
        summary = "Get all messages sent to the currently logged-in user.",
        description = "Gets all messages sent to the currently logged-in user ordered from oldest to newest.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully got messages.",
                content = [
                    Content(
                        array = ArraySchema(
                            schema = Schema(
                                implementation = MessageDTO::class
                            )
                        )
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value = ""
                            )
                        ]
                    )
                ]
            )
        ]
    )
    @GetMapping("/messages")
    fun getMessages(
        @AuthenticationPrincipal principal: Mono<UserDetails>
    ): Mono<ResponseEntity<List<MessageDTO>>> =
        principal
            .flatMap { userDetails ->

                userRepo
                    .findUserByUsername(userDetails.username)
                    .mapNotNull { it.id }
                    .flatMap { currentUserId ->
                        if (currentUserId != null) {
                            messageRepo
                                .findAllByRecipientIdOrderByCreatedAsc(currentUserId)
                                .flatMap { msg ->

                                    userRepo
                                        .findById(msg.senderId)
                                        .mapNotNull { sender ->
                                            MessageDTO(
                                                created = msg.created,
                                                sender = sender.username,
                                                recipient = userDetails.username,
                                                message = msg.message,
                                            )
                                        }
                                }
                                .collectList()
                        } else {
                            Mono.empty()
                        }
                    }
                    .switchIfEmpty {
                        Mono.error(
                            CustomException(
                                "The current user was not found. Please try to log in again.",
                                HttpStatus.INTERNAL_SERVER_ERROR
                            )
                        )
                    }
            }
            .map {
                ResponseEntity(
                    it,
                    HttpStatus.OK
                )
            }
}