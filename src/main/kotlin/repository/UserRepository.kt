package com.uniface.repository

import com.uniface.dto.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {

    // Login qilish jarayonida kerak bo'ladi
    fun findByUsername(username: String): User?

    // Admin ustoz qo'shganda dublikat bo'lmasligi uchun
    fun existsByUsername(username: String): Boolean
}