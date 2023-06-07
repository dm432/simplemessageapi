package com.danielmaile.simplemessageapi.web

import com.danielmaile.simplemessageapi.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1")
class CurrentUserController {

    @Autowired
    private lateinit var userRepo: UserRepository

    @GetMapping("/me")
    fun currentUser(@AuthenticationPrincipal principal: Mono<UserDetails>) =
        principal
            .flatMap { userRepo.findUserByUsername(it.username) }
}