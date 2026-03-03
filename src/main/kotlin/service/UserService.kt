package com.uniface.service

import com.uniface.data.Role
import com.uniface.entity.User
import com.uniface.dto.UserDto // Controller'dan keladigan ma'lumotlar
import com.uniface.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    // 1. Yangi ustozni saqlash (DTO qabul qiladi)
    fun saveTeacher(request: UserDto): User {
        if (userRepository.existsByUsername(request.username)) {
            throw RuntimeException("Bu login band, boshqa tanlang!")
        }

        val newUser = User(
            fullName = request.fullName,
            username = request.username,
            password = passwordEncoder.encode(request.password), // Shifrlash
            role = Role.ROLE_TEACHER
        )
        return userRepository.save(newUser)
    }

    // 2. Ustoz ma'lumotlarini yangilash (DTO qabul qiladi)
    fun updateUser(id: Long, request: UserDto): User {
        val user = userRepository.findById(id).orElseThrow {
            RuntimeException("Ustoz topilmadi")
        }

        user.fullName = request.fullName

        // Agar yangi parol kelsa va bo'sh bo'lmasa - yangilaymiz
        if (!request.password.isNullOrBlank()) {
            user.password = passwordEncoder.encode(request.password)
        }

        return userRepository.save(user)
    }
}