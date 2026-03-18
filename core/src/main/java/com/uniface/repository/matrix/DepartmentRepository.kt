package com.uniface.repository.matrix

import com.uniface.entity.matrix.Department
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DepartmentRepository : JpaRepository<Department, Long> {
    fun findByName(name: String): Department?
    fun findAllByFacultyId(facultyId: Long): List<Department>
}