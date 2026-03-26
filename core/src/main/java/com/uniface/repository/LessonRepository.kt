package com.uniface.repository

import com.uniface.entity.Lesson
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LessonRepository : JpaRepository<Lesson, Long> {
    // O'qituvchining hozirgi faol darsini topish
    fun findByTeacherIdAndIsActiveTrue(teacherId: Long): Lesson?
}