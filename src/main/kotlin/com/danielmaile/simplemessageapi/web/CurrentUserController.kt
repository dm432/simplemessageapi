package com.danielmaile.simplemessageapi.web

import com.danielmaile.simplemessageapi.exception.UsernameNotFoundException
import com.danielmaile.simplemessageapi.model.User
import com.danielmaile.simplemessageapi.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.RestController

@RestController
class CurrentUserController : CurrentUserAPI {

    @Autowired
    private lateinit var userRepo: UserRepository

    override suspend fun currentUser(@AuthenticationPrincipal principal: UserDetails): User =
        userRepo.findUserByUsername(principal.username)
            ?: throw UsernameNotFoundException(principal.username)
}
