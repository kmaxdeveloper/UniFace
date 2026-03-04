package com.uniface.service

import com.uniface.dto.LoginRequest
import com.uniface.security.JwtUtils
import com.uniface.repository.UserRepository // Qo'shildi
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils,
    private val userRepository: UserRepository // UserRepository-ni inject qildik
) {
    // Endi faqat String emas, Map qaytaradigan qilamiz
    fun authenticate(request: LoginRequest): Map<String, String> {
        // 1. Login va parolni tekshiradi
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )

        // 2. Bazadan foydalanuvchini topamiz (rolini olish uchun)
        val user = userRepository.findByUsername(request.username)
            ?: throw RuntimeException("User topilmadi")

        // 3. Token yasaymiz
        val token = jwtUtils.generateToken(request.username)

        // 4. Token va Rol-ni birga qaytaramiz
        return mapOf(
            "token" to token,
            "role" to user.role.name, // Role shu yerda ketadi (masalan: ROLE_ADMIN)
            "username" to user.username
        )
    }
}