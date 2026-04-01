package com.uniface.repository

import com.uniface.entity.StudentGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface GroupRepository : JpaRepository<StudentGroup, Long> {

    // 1. Nom bo'yicha qidirish (masalan: "611-21")
    fun findByName(name: String): StudentGroup?

    // 2. Muayyan fakultetga tegishli barcha guruhlarni olish
    fun findAllByFaculty(faculty: String): List<StudentGroup>

    // 3. O'qituvchiga biriktirilgan guruhlarni ID orqali olish (Custom Query)
    @Query("""
        SELECT g FROM StudentGroup g 
        JOIN g.teachers t 
        WHERE t.id = :teacherId
    """)
    fun findAllByTeacherId(teacherId: Long): List<StudentGroup>

    // 4. Guruh mavjudligini tekshirish
    fun existsByName(name: String): Boolean
}