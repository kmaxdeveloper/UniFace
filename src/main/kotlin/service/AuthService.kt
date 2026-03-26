package com.uniface.service

import com.uniface.data.Role
import com.uniface.dto.LoginRequest
import com.uniface.repository.StudentRepository
import com.uniface.repository.TeacherRepository
import com.uniface.security.JwtUtils
import com.uniface.repository.UserRepository // Qo'shildi
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils,
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository, // 🆕 Qo'shildi
    private val teacherRepository: TeacherRepository   // 🆕 Qo'shildi
) {
    fun authenticate(request: LoginRequest): Map<String, Any> {
        // 1. Login va parolni tekshirish
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )

        // 2. Foydalanuvchini topish
        val user = userRepository.findByUsername(request.username)
            ?: throw RuntimeException("Foydalanuvchi topilmadi")

        val token = jwtUtils.generateToken(request.username)

        // 3. Javob (Response) tayyorlash
        val response = mutableMapOf<String, Any>(
            "token" to token,
            "role" to user.role.name,
            "username" to user.username,
            "userId" to (user.id ?: 0)
        )

        // 4. Role-ga qarab qo'shimcha ID-larni qo'shamiz
        when (user.role) {
            Role.ROLE_STUDENT -> {
                val student = studentRepository.findByUser(user)
                response["studentId"] = student?.studentId ?: ""
                response["fullName"] = student?.fullName ?: ""
            }
            Role.ROLE_TEACHER -> {
                val teacher = teacherRepository.findByUser(user)
                response["teacherId"] = teacher?.id ?: 0
                response["fullName"] = teacher?.fullName ?: ""
            }
            else -> {
                response["adminId"] = user.id ?: 0
            }
        }

        return response
    }
}