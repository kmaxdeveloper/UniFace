package com.uniface.controller.login

import com.uniface.dto.LoginRequest
import com.uniface.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<Any> {
        // Endi 'data' ichida ham token, ham role bor
        val authData = authService.authenticate(loginRequest)
        return ResponseEntity.ok(authData)
    }
}