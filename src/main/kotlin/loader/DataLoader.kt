package com.uniface.loader

import com.uniface.data.Role
import com.uniface.entity.User
import com.uniface.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataLoader(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        // O'zgaruvchiga olib qo'yamiz, adashmaslik uchun
        val adminUsername = "mainAdmin"

        // Tekshirish ham, saqlash ham bir xil login bilan bo'lishi shart!
        if (userRepository.findByUsername(adminUsername) == null) {
            val admin = User(
                username = adminUsername,
                password = passwordEncoder.encode("mainAdmin"),
                fullName = "Asosiy Admin",
                role = Role.ROLE_ADMIN
            )
            userRepository.save(admin)
            println("✅ Default Admin yaratildi: login: $adminUsername, parol: mainAdmin")
        } else {
            println("ℹ️ Admin ($adminUsername) allaqachon bazada bor, qayta yaratilmadi.")
        }
    }
}