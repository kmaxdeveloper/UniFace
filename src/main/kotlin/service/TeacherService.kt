package com.uniface.service

import com.uniface.entity.SubjectAllocation
import com.uniface.repository.SubjectAllocationRepository
import com.uniface.repository.TeacherRepository
import org.springframework.stereotype.Service

@Service
class TeacherService(
    private val allocationRepository: SubjectAllocationRepository,
    private val teacherRepository: TeacherRepository
) {
    // Ustozga biriktirilgan barcha fanlar va guruhlarni olish
    fun getMyAllocations(teacherId: Long): List<SubjectAllocation> {
        return allocationRepository.findAllByTeacherId(teacherId)
    }

    // Ustozning profil ma'lumotlarini olish
    fun getTeacherProfile(teacherId: Long) = teacherRepository.findById(teacherId)
}