package com.danielmaile.simplemessageapi.model

import org.springframework.security.core.GrantedAuthority

enum class Role : GrantedAuthority {
    ROLE_USER,
    ROLE_ADMIN;

    override fun getAuthority() = name
}
