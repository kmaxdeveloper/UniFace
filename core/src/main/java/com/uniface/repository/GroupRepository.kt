package com.uniface.repository

import com.uniface.entity.StudentGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface GroupRepository : JpaRepository<StudentGroup, Long> {

    // 1. Nom bo'yicha qidirish (masalan: "611-21")
    fun findByName(name: String): StudentGroup?

    @Query("SELECT g FROM StudentGroup g WHERE g.faculty.name = :facultyName")
    fun findAllByFacultyName(facultyName: String): List<StudentGroup>

    // Agar metodni nomi bo'yicha ishlatmoqchi bo'lsang:
    fun findAllByFaculty_Name(name: String): List<StudentGroup>

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