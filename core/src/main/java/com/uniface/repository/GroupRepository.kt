package com.uniface.repository

import com.uniface.entity.StudentGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface GroupRepository : JpaRepository<StudentGroup, Long> {

    fun findByName(name: String): StudentGroup?

    fun findAllByFaculty_Name(name: String): List<StudentGroup>

    fun existsByName(name: String): Boolean

    fun findAllByFacultyId(facultyId: Long): List<StudentGroup>
}