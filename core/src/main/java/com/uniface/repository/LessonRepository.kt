package com.uniface.repository

import com.uniface.entity.Lesson
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface LessonRepository : JpaRepository<Lesson, Long> {

    // 1. O'qituvchining hozirgi faol darsini topish (Siz yozganingiz)
    fun findByTeacherIdAndIsActiveTrue(teacherId: Long): Lesson?

    // 2. O'qituvchi username orqali faol darsni topish (Service-da username ishlatganimiz uchun)
    fun findByTeacherUserUsernameAndIsActiveTrue(username: String): Lesson?

    // 3. Ma'lum bir guruh uchun hozirda faol dars bor-yo'qligini tekshirish
    // (Talaba skaner qilganda o'sha guruh darsi rostdan ham ochiqligini tekshirish uchun)
    @Query("SELECT l FROM Lesson l JOIN l.groups g WHERE g.id = :groupId AND l.isActive = true")
    fun findActiveLessonByGroupId(groupId: Long): Lesson?

    // 4. Ma'lum bir vaqt oralig'ida o'tilgan darslarni topish (Hisobotlar uchun)
    fun findAllByStartTimeBetween(start: LocalDateTime, end: LocalDateTime): List<Lesson>

    // 5. O'qituvchining barcha o'tgan darslari ro'yxati
    fun findAllByTeacherIdOrderByStartTimeDesc(teacherId: Long): List<Lesson>

    // 6. Avtomatik yopish (Cleanup task) uchun: 2 soatdan beri ochiq qolib ketgan darslarni topish
    fun findAllByIsActiveTrueAndStartTimeBefore(time: LocalDateTime): List<Lesson>

    fun findAllByGroupsId(groupId: Long): List<Lesson>
    fun existsByRoomIdAndTimeslotId(roomId: Long, timeslotId: Long): Boolean

    fun findByGroups_Name(groupName: String): List<Lesson>

    // Ustoz ID si bo'yicha darslarni topish
    fun findByTeacher_Id(teacherId: Long): List<Lesson>
}