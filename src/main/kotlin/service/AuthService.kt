package com.uniface.service

import com.uniface.dto.LoginRequest
import com.uniface.security.JwtUtils
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils
) {
    fun authenticate(request: LoginRequest): String {
        // 1. Login va parolni tekshiradi (xato bo'lsa Exception otadi)
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )

        // 2. Hammasi to'g'ri bo'lsa, Token yasab qaytaradi
        return jwtUtils.generateToken(request.username)
    }
}