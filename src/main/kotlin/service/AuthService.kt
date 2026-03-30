package com.uniface.service

import com.uniface.data.Role
import com.uniface.dto.LoginRequest
import com.uniface.repository.StudentRepository
import com.uniface.repository.TeacherRepository
import com.uniface.security.JwtUtils
import com.uniface.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils,
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
    private val teacherRepository: TeacherRepository
) {
    fun authenticate(request: LoginRequest): Map<String, Any> {
        // 1. Login va parolni Spring Security orqali tekshirish
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )

        // 2. Foydalanuvchini bazadan olish
        val user = userRepository.findByUsername(request.username)
            ?: throw UsernameNotFoundException("Foydalanuvchi topilmadi")

        // ✅ MUHIM: Foydalanuvchi rolini List ko'rinishida tayyorlaymiz
        // Chunki JwtUtils endi ro'yxat kutmoqda: generateToken(username, roles)
        val userRoles = listOf(user.role.name)

        // 3. Tokenni rollar bilan birga generatsiya qilish
        val token = jwtUtils.generateToken(user.username, userRoles)

        // 4. Response (Javob) tayyorlash
        val response = mutableMapOf<String, Any>(
            "token" to token,
            "role" to user.role.name,
            "username" to user.username,
            "userId" to (user.id ?: 0)
        )

        // 5. Role-ga qarab profil ma'lumotlarini qo'shish
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
            Role.ROLE_ADMIN -> {
                response["adminId"] = user.id ?: 0
                response["fullName"] = "Administrator"
            }

            else -> {
                response["message"] = "Tizimga kirish cheklangan"
            }
        }

        return response
    }
}