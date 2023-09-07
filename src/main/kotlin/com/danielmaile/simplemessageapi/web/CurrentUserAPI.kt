package com.danielmaile.simplemessageapi.web

import com.danielmaile.simplemessageapi.model.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@RequestMapping("/api/v1")
@Tag(name = "Current User Information")
interface CurrentUserAPI {

    @Operation(
        summary = "Information about the currently logged-in user.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved information about the current logged-in unser"
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
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    suspend fun currentUser(@AuthenticationPrincipal principal: UserDetails): User
}
