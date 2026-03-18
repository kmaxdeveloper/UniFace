package com.uniface.repository.matrix

import com.uniface.entity.matrix.Faculty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FacultyRepository : JpaRepository<Faculty, Long> {
    fun findByName(name: String): Faculty?
}