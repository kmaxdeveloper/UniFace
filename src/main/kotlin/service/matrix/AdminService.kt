package com.uniface.service.matrix

import com.uniface.entity.matrix.Department
import com.uniface.repository.matrix.DepartmentRepository
import com.uniface.repository.matrix.FacultyRepository
import org.springframework.stereotype.Service

@Service
class AdminService(
    private val departmentRepository: DepartmentRepository,
    private val facultyRepository: FacultyRepository
) {

    // Yangi kafedra qo'shish
    fun addDepartment(name: String, facultyId: Long): Department {
        val faculty = facultyRepository.findById(facultyId)
            .orElseThrow { RuntimeException("Fakultet topilmadi!") }

        val department = Department(name = name, faculty = faculty)
        return departmentRepository.save(department)
    }

    // 1. Fakultet ID bo'yicha kafedralarni olish
    fun getDepartmentsByFaculty(facultyId: Long): List<Department> {
        return departmentRepository.findAllByFacultyId(facultyId)
    }

    // 2. Barcha kafedralarni olish
    fun getAllDepartments(): List<Department> {
        return departmentRepository.findAll()
    }
}