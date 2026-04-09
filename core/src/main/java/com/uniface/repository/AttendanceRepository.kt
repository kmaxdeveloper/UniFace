package com.uniface.repository

import com.uniface.entity.Attendance
import com.uniface.entity.Lesson
import com.uniface.entity.Student
import com.uniface.entity.Subject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import org.springframework.stereotype.Repository

@Repository
interface AttendanceRepository : JpaRepository<Attendance, Long> {

    // Talaba studentId bo'yicha ✅
    fun findByStudentStudentId(studentId: String): List<Attendance>

    // Guruh bo'yicha ✅
    fun findByGroupId(groupId: Long): List<Attendance>

    // Fan bo'yicha ✅
    fun findBySubjectId(subjectId: Long): List<Attendance>

    // Bugun tekshirish ✅
    @Query("""
        SELECT COUNT(a) > 0 FROM Attendance a 
        WHERE a.student = :student 
        AND a.subject = :subject 
        AND a.timestamp >= :startOfDay 
        AND a.timestamp <= :endOfDay
    """)
    fun existsByStudentAndSubjectToday(
        @Param("student") student: Student,
        @Param("subject") subject: Subject?,
        @Param("startOfDay") startOfDay: LocalDateTime,
        @Param("endOfDay") endOfDay: LocalDateTime
    ): Boolean

    // Guruh + fan bo'yicha ✅
    @Query("""
        SELECT a FROM Attendance a 
        WHERE a.group.id = :groupId 
        AND a.subject.id = :subjectId
    """)
    fun findByGroupAndSubject(
        @Param("groupId") groupId: Long,
        @Param("subjectId") subjectId: Long
    ): List<Attendance>

    // Sana oralig'ida ✅
    @Query("""
        SELECT a FROM Attendance a 
        WHERE a.timestamp >= :start 
        AND a.timestamp <= :end
    """)
    fun findByDateRange(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Attendance>

    // 1. Spring Data JPA nomlash standarti orqali (Avtomatik ishlaydi)
    fun existsByStudentAndLesson(student: Student, lesson: Lesson): Boolean

    // 2. Yoki aniqroq ID-lar bo'yicha tekshirish (Service-da qulayroq bo'lishi mumkin)
    fun existsByStudentStudentIdAndLessonId(studentId: String, lessonId: Long): Boolean

    fun countByDateAndIsPresentTrue(date: java.time.LocalDate): Long

    // Native Query orqali eng ko'p kelmaganlarni topish
    @Query(value = """
        SELECT s.full_name, COUNT(a.id) as absent_count 
        FROM attendance a 
        JOIN students s ON a.student_id = s.id 
        WHERE a.is_present = false 
        GROUP BY s.id 
        ORDER BY absent_count DESC 
        LIMIT :limit
    """, nativeQuery = true)
    fun findTopAbsentStudents(limit: Int): List<Any>
}