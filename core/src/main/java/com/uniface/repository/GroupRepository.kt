package com.uniface.repository

import com.uniface.entity.StudentGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface GroupRepository : JpaRepository<StudentGroup, Long> {

    fun findByName(name: String): StudentGroup?

    fun findAllByFaculty_Name(name: String): List<StudentGroup>

    // TO'G'RILANGAN VARIANT:
    // StudentGroup-da 'teachers' yo'qligi uchun, so'rovni Teacher orqali yuboramiz
    @Query("SELECT t.groups FROM Teacher t WHERE t.id = :teacherId")
    fun findAllByTeacherId(teacherId: Long): List<StudentGroup>

    fun existsByName(name: String): Boolean

    fun findAllByFacultyId(facultyId: Long): List<StudentGroup>
}