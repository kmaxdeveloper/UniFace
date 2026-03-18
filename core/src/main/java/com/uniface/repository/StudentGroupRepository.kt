package com.uniface.repository

import com.uniface.entity.StudentGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StudentGroupRepository : JpaRepository<StudentGroup, Long> {

    // Guruh nomi bo'yicha qidirish (Import paytida dublikat bo'lmasligi uchun kerak)
    fun findByName(name: String): StudentGroup?

    // Agar kerak bo'lsa, ma'lum bir kursdagi guruhlarni topish
    fun findAllByCourse(course: Int): List<StudentGroup>
    // Fakultetga tegishli barcha guruhlarni olish uchun
    fun findAllByFacultyId(facultyId: Long): List<StudentGroup>
}