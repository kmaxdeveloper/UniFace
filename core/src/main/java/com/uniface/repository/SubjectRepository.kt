package com.uniface.repository

import com.uniface.entity.Subject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubjectRepository : JpaRepository<Subject, Long> {
    fun findByName(name: String): Subject?
    fun findByCode(code: String): Subject?
    fun findAllByDepartmentId(deptId: Long): List<Subject>
    //fun findByTeacherId(teacherId : Long) : List<Subject>
    fun findAllByTeacherId(teacherId: Long): List<Subject> // O'qituvchi ID bo'yicha topish
}