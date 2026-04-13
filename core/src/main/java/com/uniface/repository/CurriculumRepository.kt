package com.uniface.repository

import com.uniface.entity.Curriculum
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CurriculumRepository : JpaRepository<Curriculum, Long> {

    /**
     * Ma'lum bir guruh uchun o'quv rejasini topish
     */
    fun findByGroupId(groupId: Long): List<Curriculum>

    /**
     * Ma'lum bir semestrdagi barcha fanlar va guruhlar rejasini topish
     */
    fun findBySemester(semester: Int): List<Curriculum>

    /**
     * Muayyan fanning o'quv rejasida bor-yo'qligini tekshirish
     */
    fun findBySubjectId(subjectId: Long): List<Curriculum>

    /**
     * Ma'lum bir guruh va semestr bo'yicha o'quv rejasini topish
     */
    fun findByGroupIdAndSemester(groupId: Long, semester: Int): List<Curriculum>
}