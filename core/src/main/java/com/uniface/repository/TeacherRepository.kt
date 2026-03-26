package com.uniface.repository

import com.uniface.entity.Teacher
import com.uniface.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TeacherRepository : JpaRepository<Teacher, Long> {
    fun findByUser(user: User): Teacher?

    // 1. User ID orqali o'qituvchi profilini topish (Login tizimi uchun eng muhimi)
    fun findByUserId(userId: Long): Teacher?

    // 2. Ismi bo'yicha qidirish (Adminlar uchun)
    fun findByFullNameContainingIgnoreCase(fullName: String): List<Teacher>

    // 3. O'qituvchining barcha fanlarini olish (Mizan AI va UniFace uchun)
    @Query("SELECT t.subjects FROM Teacher t WHERE t.id = :teacherId")
    fun findSubjectsByTeacherId(@Param("teacherId") teacherId: Long): Set<com.uniface.entity.Subject>

    // 4. O'qituvchining barcha guruhlarini olish (UniFace dars boshlash uchun)
    @Query("SELECT t.groups FROM Teacher t WHERE t.id = :teacherId")
    fun findGroupsByTeacherId(@Param("teacherId") teacherId: Long): Set<com.uniface.entity.StudentGroup>

    // 5. Fan bo'yicha o'qituvchilarni topish (Mizan AI'da topshiriqni kim berganini aniqlashda)
    @Query("SELECT t FROM Teacher t JOIN t.subjects s WHERE s.id = :subjectId")
    fun findAllBySubjectId(@Param("subjectId") Long: Long): List<Teacher>
}