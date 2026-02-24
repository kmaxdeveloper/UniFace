package com.uniface.repository

import com.uniface.entity.Student
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StudentRepository : JpaRepository<Student, String> {
    fun findByFaceId(faceId: String): Student?
}