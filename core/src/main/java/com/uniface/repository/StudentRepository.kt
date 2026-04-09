package com.uniface.repository

import com.uniface.entity.Student
import com.uniface.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository interface StudentRepository : JpaRepository<Student, String> {
    fun findByUser(user: User): Student?
    fun findByFaceId(faceId: String): Student?

    fun findByStudentId(studentId: String): Student?

    fun findByGroupId(groupId: Long): List<Student>

    @Query("SELECT COUNT(s) FROM Student s WHERE s.group.id = :groupId")
    fun countByGroupId(@Param("groupId") groupId: Long): Long

    // User ID orqali talabani topish (OneToOne bog'liqlik uchun)
    fun findByUserId(userId: Long): Student?

    // Guruh nomi bo'yicha qidirish (Admin paneli uchun foydali)
    fun findAllByGroupName(groupName: String): List<Student>
    fun findAllByGroupId(groupId: Long): List<Student>

    fun findAllByFullNameContainingIgnoreCase(name: String): List<Student>
}