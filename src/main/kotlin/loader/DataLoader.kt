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
        if (userRepository.findByUsername("admin") == null) {
            val admin = User(
                username = "admin_komil",
                password = passwordEncoder.encode("admin_komil2001"), // Default parol
                fullName = "Asosiy Admin",
                role = Role.ROLE_ADMIN
            )
            userRepository.save(admin)
            println("Default Admin yaratildi: login: admin, parol: admin123")
        }
    }
}