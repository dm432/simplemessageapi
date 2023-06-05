package com.danielmaile.simplemessageapi.web

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1")
class CurrentUserController {

    @GetMapping("/me")
    fun currentUser(@AuthenticationPrincipal principal: Mono<UserDetails>): Mono<Map<String, Any>> {
        return principal
            .map { user ->
                mapOf(
                    "name" to user.username,
                    "roles" to AuthorityUtils.authorityListToSet(user.authorities) // TODO why are roles always empty?
                )
            }
    }
}