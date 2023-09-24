package com.danielmaile.simplemessageapi.web

import com.danielmaile.simplemessageapi.web.model.NewMessage
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus

@RequestMapping("api/v1")
@Tag(name = "Message")
interface MessageAPI {

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
                        examples = [
                            ExampleObject(
                                value = ""
                            )
                        ]
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
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createMessage(
        @AuthenticationPrincipal principal: UserDetails,
        @RequestBody message: NewMessage
    ): MessageDTO

    @Operation(
        summary = "Get all messages sent to the currently logged-in user.",
        description = "Gets all messages sent to the currently logged-in user ordered from oldest to newest.",
        parameters = [
            Parameter(
                name = "page",
                required = false,
                schema = Schema(
                    defaultValue = "0"
                )
            ),
            Parameter(
                name = "size",
                required = false,
                schema = Schema(
                    defaultValue = "10"
                )
            ),
            Parameter(
                name = "unreadOnly",
                description = "If set to true only unread messages will be returned.",
                required = false,
                schema = Schema(
                    type = "boolean",
                    defaultValue = "false"
                )
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully got messages.",
                content = [
                    Content(
                        array = ArraySchema(
                            schema = Schema(
                                implementation = Page::class
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
    @ResponseStatus(HttpStatus.OK)
    suspend fun getMessages(
        @AuthenticationPrincipal principal: UserDetails,
        @RequestParam(required = false, value = "page", defaultValue = "0") page: Int,
        @RequestParam(required = false, value = "size", defaultValue = "10") size: Int,
        @RequestParam(required = false, value = "unreadOnly", defaultValue = "false") unreadOnly: Boolean
    ): Page<MessageDTO>

    @Operation(
        summary = "Read all messages.",
        description = "Sets the read flag of all messages received from the currently logged-in user to true.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully read all messages.",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                value = ""
                            )
                        ]
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
    @PostMapping("/messages/read-all")
    @ResponseStatus(HttpStatus.OK)
    suspend fun readAllMessages(
        @AuthenticationPrincipal principal: UserDetails
    )
}
