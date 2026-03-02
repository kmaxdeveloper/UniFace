package com.uniface.service

import com.uniface.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("Foydalanuvchi topilmadi: $username")

        // Bazadagi userni Spring Security tushunadigan formatga o'giramiz
        return User(
            user.username,
            user.password,
            listOf(SimpleGrantedAuthority(user.role.name)) // ROLE_ADMIN yoki ROLE_TEACHER
        )
    }
}