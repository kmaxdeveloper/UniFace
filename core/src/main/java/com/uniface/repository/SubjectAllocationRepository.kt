package com.uniface.repository

import com.uniface.entity.SubjectAllocation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SubjectAllocationRepository : JpaRepository<SubjectAllocation, Long> {

    // 1. Ustozga tegishli barcha darslarni topish
    fun findAllByTeacherId(teacherId: Long): List<SubjectAllocation>

    // 2. Muayyan guruhga tegishli barcha fanlarni topish
    fun findAllByGroupId(groupId: Long): List<SubjectAllocation>

    // 3. Patok nomi bo'yicha qidirish (Masalan: "940-21 Patok")
    fun findAllByPatokName(patokName: String): List<SubjectAllocation>

    // 4. Murakkab filtr: Ustoz ma'lum bir guruhga ma'lum bir fandan dars o'tadimi?
    @Query("""
        SELECT s FROM SubjectAllocation s 
        WHERE s.teacher.id = :teacherId 
        AND s.subject.id = :subjectId 
        AND (s.group.id = :groupId OR s.patokName = :patokName)
    """)
    fun findSpecificAllocation(
        @Param("teacherId") teacherId: Long,
        @Param("subjectId") subjectId: Long,
        @Param("groupId") groupId: Long?,
        @Param("patokName") patokName: String?
    ): SubjectAllocation?

    // 5. Ustoz hozir dars o'tishi kerak bo'lgan guruhlar ro'yxatini olish
    @Query("SELECT s.group.id FROM SubjectAllocation s WHERE s.teacher.id = :teacherId AND s.subject.id = :subjectId")
    fun findGroupIdsByTeacherAndSubject(
        @Param("teacherId") teacherId: Long,
        @Param("subjectId") subjectId: Long
    ): List<Long>
}