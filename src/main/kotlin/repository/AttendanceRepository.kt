package com.uniface.repository

import com.uniface.entity.Attendance
import com.uniface.entity.Student
import com.uniface.entity.Subject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import org.springframework.stereotype.Repository

@Repository
interface AttendanceRepository : JpaRepository<Attendance, Long> {

    fun findByStudentStudentId(studentId: String): List<Attendance>

    // Guruh bo'yicha davomat
    fun findByGroupId(groupId: Long): List<Attendance>

    // Fan bo'yicha davomat
    fun findBySubjectId(subjectId: Long): List<Attendance>

    // Talaba bo'yicha davomat
    fun findByStudentId(studentId: Long): List<Attendance>

    // Bugun tekshirish
    @Query("""
        SELECT COUNT(a) > 0 FROM Attendance a 
        WHERE a.student = :student 
        AND a.subject = :subject 
        AND a.timestamp >= :startOfDay 
        AND a.timestamp <= :endOfDay
    """)
    fun existsByStudentAndSubjectToday(
        @Param("student") student: Student,
        @Param("subject") subject: Subject,
        @Param("startOfDay") startOfDay: LocalDateTime,
        @Param("endOfDay") endOfDay: LocalDateTime
    ): Boolean

    // Guruh + fan bo'yicha
    @Query("""
        SELECT a FROM Attendance a 
        WHERE a.group.id = :groupId 
        AND a.subject.id = :subjectId
    """)
    fun findByGroupAndSubject(
        @Param("groupId") groupId: Long,
        @Param("subjectId") subjectId: Long
    ): List<Attendance>

    // Sana oralig'ida
    @Query("""
        SELECT a FROM Attendance a 
        WHERE a.timestamp >= :start 
        AND a.timestamp <= :end
    """)
    fun findByDateRange(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Attendance>

    // Guruh umumiy talabalar soni (davomat %)
    @Query("""
        SELECT COUNT(DISTINCT a.student.id) FROM Attendance a 
        WHERE a.group.id = :groupId
        AND a.timestamp >= :start
        AND a.timestamp <= :end
    """)
    fun countDistinctStudentsByGroup(
        @Param("groupId") groupId: Long,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): Long
}