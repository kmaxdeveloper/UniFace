package com.uniface.repository

import com.uniface.entity.Subject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubjectRepository : JpaRepository<Subject, Long> {
    fun findByName(name: String): Subject?
    fun findByCode(code: String): Subject?
    fun findAllByCodeIn(codes: List<String>): List<Subject>
    fun findAllByDepartmentId(deptId: Long): List<Subject>
}